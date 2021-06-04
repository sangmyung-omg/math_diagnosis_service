package com.tmax.WaplMath.Recommend.service.schedule;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.dto.CardDTO;
import com.tmax.WaplMath.Recommend.dto.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.ProblemSetDTO;
import com.tmax.WaplMath.Recommend.dto.UkMasteryDTO;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledgeKey;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.model.user.UserExamScope;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;
import com.tmax.WaplMath.Recommend.repository.UkRelRepository;
import com.tmax.WaplMath.Recommend.repository.UserExamScopeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;
import com.tmax.WaplMath.Recommend.repository.UserRepository;

/**
 * Generate today exam learning schedule card
 * 
 * @author Sangheon Lee
 *
 */
@Service("ScheduleServiceV0")
@Primary
public class ScheduleServiceV0 implements ScheduleServiceBase {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// Hyperparameter
	private static final Integer MAX_CARD_NUM = 5;
	private static final Integer SMALL_CARD_PRE_UK_NUM = 2;
	private static final Float PRE_UK_MASTERY_THRESHOLD = 0.4f;
	private static final Float SUP_UK_MASTERY_THRESHOLD = 0.4f;
	private static final Integer MAX_SMALL_CARD_PROBLEM_NUM = 5;
	private static final Integer MAX_EXAM_CARD_PROBLEM_NUM = 15;

	// Constant
	private static final String TYPE_CARD_TYPE = "type";
	private static final String SUPPLEMENT_CARD_TYPE = "supple";
	private static final String MID_EXAM_CARD_TYPE = "midExam";
	private static final String TRIAL_EXAM_CARD_TYPE = "trialExam";
	private static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;

	// Repository
	@Autowired
	private ProblemRepo problemRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private UkRelRepository ukRelRepo;
	@Autowired
	private ProblemTypeRepo problemTypeRepo;
	@Autowired
	private UserExamScopeRepo userExamScopeRepo;
	@Autowired
	private CurriculumRepository curriculumRepo;
	@Autowired
	private ProblemUkRelRepository problemUkRelRepo;
	@Autowired
	private UserKnowledgeRepository userKnowledgeRepo;

	@Autowired
	ScheduleHistoryManager historyManager;

	public String userId;
	public String today;

	public Map<String, List<Problem>> generateDiffProbListByProb(List<Problem> probList) {
		Map<String, List<Problem>> diffProbList = new HashMap<String, List<Problem>>();
		for (Problem prob : probList) {
			String difficulty = prob.getDifficulty();
			if (diffProbList.get(difficulty) == null) {
				List<Problem> tempList = new ArrayList<Problem>();
				tempList.add(prob);
				diffProbList.put(difficulty, tempList);
			} else {
				diffProbList.get(difficulty).add(prob);
			}
		}
		return diffProbList;
	}

	public Map<String, List<Problem>> gerenateDiffProbListByUk(List<Integer> ukList, Integer CARD_UK_NUM) {
		Map<String, List<Problem>> diffProbList = new HashMap<String, List<Problem>>();
		for (Integer ukId : ukList) {
			for (String difficulty : Arrays.asList("상", "중", "하")) {
				List<Problem> probList = problemUkRelRepo.findByUkIdDifficulty(ukId, difficulty);
				if (probList.size() > 0) {
					Problem prob = probList.get(0);
					if (diffProbList.get(difficulty) == null) {
						List<Problem> tempList = new ArrayList<Problem>();
						tempList.add(prob);
						diffProbList.put(difficulty, tempList);
					} else if (diffProbList.get(difficulty).size() != CARD_UK_NUM) {
						diffProbList.get(difficulty).add(prob);
					}
				}
			}
		}
		return diffProbList;
	}

	public void printDiffProbList(Map<String, List<Problem>> diffProbList) {
		logger.info("");
		for (String key : diffProbList.keySet()) {
			List<Problem> probList = diffProbList.get(key);
			List<Integer> probIdList = new ArrayList<Integer>();
			probList.forEach(prob -> probIdList.add(prob.getProbId()));
			logger.info(String.format("%s 난이도 문제들 = " + probIdList.toString(), key));
		}
	}

	public CardDTO addProblemList(CardDTO card, Map<String, List<Problem>> diffProbList, Integer MAX_PROBLEM_NUM) {
		List<ProblemSetDTO> problemSetList = card.getProbIdSetList();
		Integer estimatedTime = card.getEstimatedTime();
		List<Problem> highProbList, middleProbList, lowProbList;
		middleProbList = diffProbList.get("중");
		highProbList = diffProbList.containsKey("상") ? diffProbList.get("상") : middleProbList;
		lowProbList = diffProbList.containsKey("하") ? diffProbList.get("하") : middleProbList;

		int probSetCnt = problemSetList.size();
		int listIdx = 0;
		Integer timeTotal = estimatedTime;
		while (true) {
			Problem highProb = highProbList.get(listIdx % highProbList.size());
			Problem middleProb = middleProbList.get(listIdx % middleProbList.size());
			Problem lowProb = lowProbList.get(listIdx % lowProbList.size());

			// add problem set
			ProblemSetDTO prob = new ProblemSetDTO(highProb.getProbId(), middleProb.getProbId(), lowProb.getProbId());
			problemSetList.add(prob);

			// calc estimated time
			Float highProbTime = highProb.getTimeRecommendation();
			Integer highEstimateTime = (highProbTime == null || highProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME
					: Math.round(highProbTime);
			Float middleProbTime = middleProb.getTimeRecommendation();
			Integer middleEstimateTime = (middleProbTime == null || middleProbTime == 0.0f)
					? AVERAGE_PROB_ESTIMATED_TIME
					: Math.round(middleProbTime);
			Float lowProbTime = lowProb.getTimeRecommendation();
			Integer lowEstimateTime = (lowProbTime == null || lowProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME
					: Math.round(lowProbTime);
			timeTotal += Math.round((highEstimateTime + middleEstimateTime + lowEstimateTime) / 3);

			probSetCnt += 1;
			listIdx += 1;
			if (probSetCnt == MAX_PROBLEM_NUM)
				break;
		}
		card.setEstimatedTime(timeTotal);
		card.setProbIdSetList(problemSetList);
		return card;
	}

	public CardDTO generateMidExamCard(String sectionId) {
		CardDTO midExamCard = new CardDTO();

		String cardTitle = curriculumRepo.findSectionName(sectionId);
		String sectionTitle = "";

		midExamCard.setCardType(MID_EXAM_CARD_TYPE);
		midExamCard.setCardTitle(cardTitle);
		midExamCard.setSectionTitle(sectionTitle);
		midExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		midExamCard.setEstimatedTime(0);

		// sectionId 내의 모든 문제 id 가져와
		List<Problem> sectionProbList = problemRepo.findAllProbBySection(sectionId);
		Map<String, List<Problem>> diffProbList = generateDiffProbListByProb(sectionProbList);
		midExamCard = addProblemList(midExamCard, diffProbList, MAX_EXAM_CARD_PROBLEM_NUM);
		return midExamCard;
	}

	public CardDTO generateSupplementCard(List<UserKnowledge> lowMasteryList) {
		CardDTO supplementCard = new CardDTO();

		String cardTitle = "";
		String sectionTitle = "";

		supplementCard.setCardType(SUPPLEMENT_CARD_TYPE);
		supplementCard.setCardTitle(cardTitle);
		supplementCard.setSectionTitle(sectionTitle);
		supplementCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		supplementCard.setEstimatedTime(0);

		List<Integer> lowMasteryUkList = new ArrayList<Integer>();
		lowMasteryList.forEach(uk -> lowMasteryUkList.add(uk.getUkId()));
		Map<String, List<Problem>> diffProbList = gerenateDiffProbListByUk(lowMasteryUkList,
				MAX_SMALL_CARD_PROBLEM_NUM);
		supplementCard = addProblemList(supplementCard, diffProbList, MAX_SMALL_CARD_PROBLEM_NUM);
		return supplementCard;
	}

	public CardDTO generateTypeCard(Integer typeId) {
		CardDTO typeCard = new CardDTO();

		String cardTitle = problemTypeRepo.findTypeNameById(typeId);
		String sectionTitle = problemTypeRepo.findSubSectionNameById(typeId);

		typeCard.setCardType(TYPE_CARD_TYPE);
		typeCard.setCardTitle(cardTitle);
		typeCard.setSectionTitle(sectionTitle);
		typeCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		typeCard.setEstimatedTime(0);

		List<Integer> typeProbIdList = problemRepo.findAllProbIdByType(typeId);
		if (typeProbIdList.size() == 0)
			return new CardDTO();

		else {
			List<Integer> ukIdList = problemUkRelRepo.findAllUkIdList(typeProbIdList);
			List<Integer> preUkIdList = ukRelRepo.findPreUkIdList(ukIdList, "선수");
			logger.info("유형카드 내 ukIdList: " + ukIdList.toString());
			logger.info("유형카드 내 preUkIdList: " + preUkIdList.toString());

			// 이해도가 낮은 애들 선별
			List<UkMasteryDTO> preUkMasteryList = new ArrayList<UkMasteryDTO>();
			for (Integer preUkId : preUkIdList) {
				UserKnowledgeKey preMasteryKey = new UserKnowledgeKey();
				preMasteryKey.setUserUuid(userId);
				preMasteryKey.setUkId(preUkId);
				UserKnowledge preUkUserKnowledge;
				try {
					preUkUserKnowledge = userKnowledgeRepo.findById(preMasteryKey)
							.orElseThrow(() -> new Exception(Integer.toString(preUkId)));
				} catch (Exception e) {
					logger.info(String.format("User mastery of ukId %s not in USER_KNOWLEDGE TB.", e.getMessage()));
					continue;
				}
				Float preUkMastery = preUkUserKnowledge.getUkMastery();
				if (preUkMastery <= PRE_UK_MASTERY_THRESHOLD)
					preUkMasteryList.add(new UkMasteryDTO(preUkId, Float.toString(preUkMastery)));
			}

			// preUk sort by uk mastery
			Collections.sort(preUkMasteryList, new Comparator<UkMasteryDTO>() {
				public int compare(UkMasteryDTO preUk1, UkMasteryDTO preUk2) {
					Float preUk1Mastery = Float.parseFloat(preUk1.getUkMastery());
					Float preUk2Mastery = Float.parseFloat(preUk2.getUkMastery());
					return preUk1Mastery.compareTo(preUk2Mastery);
				}
			});
			// preUK에 대해 triplet 구성
			Integer preUkProbNum = SMALL_CARD_PRE_UK_NUM > preUkMasteryList.size() ? preUkMasteryList.size()
					: SMALL_CARD_PRE_UK_NUM;
			List<Integer> preUkList = new ArrayList<Integer>();
			preUkMasteryList.forEach(preUkMastery -> preUkList.add(preUkMastery.getUkId()));
			logger.info("그중 이해도가 낮은 것들: " + preUkList.toString());
			Map<String, List<Problem>> diffPreProbList = gerenateDiffProbListByUk(preUkList, preUkProbNum);
			printDiffProbList(diffPreProbList);
			if (diffPreProbList.keySet().size() == 3)
				typeCard = addProblemList(typeCard, diffPreProbList, preUkProbNum);

			// type에 대해 triplet 구성
			List<Problem> typeProbList;
			List<Integer> preProbIdList = new ArrayList<Integer>();
			typeCard.getProbIdSetList().forEach(prob -> {
				preProbIdList.add(prob.getHigh());
				preProbIdList.add(prob.getMiddle());
				preProbIdList.add(prob.getLow());
			});
			if (preProbIdList.size() != 0)
				typeProbList = problemRepo.findAllProbByTypeNotInList(typeId, preProbIdList);
			else
				typeProbList = problemRepo.findAllProbByType(typeId);
			Map<String, List<Problem>> diffProbList = generateDiffProbListByProb(typeProbList);
			printDiffProbList(diffProbList);
			if (!diffProbList.containsKey("중"))
				return new CardDTO();
			typeCard = addProblemList(typeCard, diffProbList, MAX_SMALL_CARD_PROBLEM_NUM);
			return typeCard;
		}
	}

	public CardDTO generateTrialExamCard(List<String> subSectionList) {
		CardDTO trialExamCard = new CardDTO();

		String cardTitle = "";
		String sectionTitle = "";

		trialExamCard.setCardType(TRIAL_EXAM_CARD_TYPE);
		trialExamCard.setCardTitle(cardTitle);
		trialExamCard.setSectionTitle(sectionTitle);
		trialExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		trialExamCard.setEstimatedTime(0);

		List<Problem> trialExamProbList = problemRepo.findAllProbBySubSectionList(subSectionList);
		Map<String, List<Problem>> diffProbList = generateDiffProbListByProb(trialExamProbList);
		trialExamCard = addProblemList(trialExamCard, diffProbList, MAX_EXAM_CARD_PROBLEM_NUM);
		return trialExamCard;
	}

	@Override
	public ExamScheduleCardDTO getExamScheduleCard(String userId) {
		ExamScheduleCardDTO output = new ExamScheduleCardDTO();
		List<CardDTO> cardList = new ArrayList<CardDTO>();

		// today date and convert to Timestamp type
		// Timestamp todayTimestamp = Timestamp.valueOf(LocalDate.now().atStartOfDay());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String today = LocalDate.now().format(formatter);

		this.userId = userId;
		this.today = today;

		// Load user information from USER_MASTER TB
		User userInfo;
		try {
			userInfo = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			output.setMessage(String.format("userId = %s is not in USER_MASTER TB.", e.getMessage()));
			return output;
		}

		// Check whether user exam information is null
		String examType = userInfo.getExamType();
		Timestamp examStartDate = userInfo.getExamStartDate();
		Timestamp examDueDate = userInfo.getExamDueDate();
		Integer examTargetScore = userInfo.getExamTargetScore();
		if (examType == null || examStartDate == null || examDueDate == null || examTargetScore == null) {
			output.setMessage("One of user's exam infomation is null. Call ExamInfo PUT service first.");
			return output;
		}

		UserExamScope userExamScope;
		try {
			userExamScope = userExamScopeRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			output.setMessage(String.format("userId = %s is not in USER_EXAM_SCOPE TB.", e.getMessage()));
			return output;
		}
		Integer startSeq = userExamScope.getStartCurriculum().getCurriculumSequence();
		Integer endSeq = userExamScope.getEndCurriculum().getCurriculumSequence();
		List<String> subSectionList = curriculumRepo.findExamSubSectionList(startSeq, endSeq); // 시험범위

		List<String> totalSectionList = problemTypeRepo.findAllSection(subSectionList);
		Set<String> totalSectionSet = new HashSet<String>(totalSectionList); // 토탈 범위 중 단원들
		logger.info("1. 시험범위 전체 중 단원들 : " + totalSectionSet.toString());

		List<Integer> completedTypeIdList;
		try {
			completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		logger.info("2. 유저가 푼 유형 UK들 : " + completedTypeIdList.toString());

		List<String> notCompletedSectionList = new ArrayList<String>();
		if (completedTypeIdList.size() == 0) {
			logger.info("- 푼게 하나도 없음.");
			notCompletedSectionList.addAll(totalSectionList);
		} else
			notCompletedSectionList = problemTypeRepo.findAllSectionNotInTypeList(subSectionList, completedTypeIdList);
		Set<String> notCompletedSectionSet = new HashSet<String>(notCompletedSectionList); // 안푼 단원들
		logger.info("3. 안푼 중단원들 : " + notCompletedSectionSet.toString());

		totalSectionSet.removeAll(notCompletedSectionSet); // totalSectionSet에 완벽히 푼 단원들 저장됨
		logger.info("4. 완벽히 유형카드를 중 푼 단원들 : " + totalSectionSet.toString());

		List<String> completedSectionIdList;
		try {
			completedSectionIdList = historyManager.getCompletedSectionIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		Set<String> completedSectionSet = new HashSet<String>(completedSectionIdList);
		logger.info("5. 이미 중간평가 카드 푼 중 단원들 : " + completedSectionSet.toString());

		totalSectionSet.removeAll(completedSectionSet); // totalSectionSet에 완벽히 푼 단원들 - 이미 푼 중간평가 저장됨
		logger.info("6. 중간평가 대상인 최종 중 단원 : " + totalSectionSet.toString());

		// 완벽히 푼 단원이 있다? 중간평가 할차례!
		if (totalSectionSet.size() != 0) {
			String sectionId = totalSectionSet.iterator().next();
			logger.info("\n중간에 다 풀었으니까 중간평가 진행: " + sectionId);

			// 카드 생성
			CardDTO midExamCard = generateMidExamCard(sectionId);
			// 결과 추가
			cardList.add(midExamCard);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		}

		// 유형 UK (혹은 보충 UK)
		// 보충 필요한지 판단
		List<Integer> suppleUkIdList;
		try {
			suppleUkIdList = historyManager.getCompletedSuppleUkIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		logger.info("7. 지금까지 보충 카드로 풀어본 ukID 리스트 = ");
		logger.info(suppleUkIdList.toString());

		List<Integer> solvedUkIdList;
		try {
			solvedUkIdList = historyManager.getSolvedUkIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		List<UserKnowledge> lowMasteryList = userKnowledgeRepo.findAllLowMasteryUkUuid(userId, solvedUkIdList,
				suppleUkIdList, SUP_UK_MASTERY_THRESHOLD);

		logger.info("8. 보충 카드 제외, 유형 카드로 추천된 ukID의 이해도 리스트 = ");
		for (UserKnowledge lowMastery : lowMasteryList)
			logger.info(String.format("   ukId = %s, mastery = %f", lowMastery.getUkId(), lowMastery.getUkMastery()));

		// 보충 채울만큼 넉넉하면 보충 카드
		if (lowMasteryList.size() >= MAX_SMALL_CARD_PROBLEM_NUM) {
			logger.info("	이해도 낮은게 많아서 보충 카드 진행");
			CardDTO supplementCard = generateSupplementCard(lowMasteryList);
			cardList.add(supplementCard);
		}

		// 나머지 카드들 유형카드로 채우기
		List<Integer> remainTypeIdList;
		if (completedTypeIdList.size() == 0)
			remainTypeIdList = problemTypeRepo.findAllExamTypeIdList(subSectionList);
		else
			remainTypeIdList = problemTypeRepo.findRemainTypeIdList(subSectionList, completedTypeIdList);

		List<Integer> noProbTypeList = new ArrayList<Integer>();
		// 공부 안한 유형uk가 있으면 유형카드
		if (remainTypeIdList.size() != 0) {
			for (Integer typeId : remainTypeIdList) {
				logger.info("\n중간평가 아니니까 유형 UK 카드 진행: " + typeId);
				CardDTO typeCard;
				typeCard = generateTypeCard(typeId);
				// 문제가 하나도 없으면 뛰어넘자
				if (typeCard.getCardType() == null) {
					noProbTypeList.add(typeId);
					continue;
				}
				cardList.add(typeCard);
				if (cardList.size() == MAX_CARD_NUM)
					break;
			}
			System.out.println(noProbTypeList);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		}

		// 보충카드 한장만 구성된 경우, 한장만 제공
		if (cardList.size() != 0) {
			System.out.println(noProbTypeList);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		} else {
			logger.info("	다 풀어서 모의고사 카드 진행. ");
			CardDTO trialExamCard = generateTrialExamCard(subSectionList);
			cardList.add(trialExamCard);

			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		}
	}
}
