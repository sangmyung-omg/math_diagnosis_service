package com.tmax.WaplMath.Recommend.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.model.CardDTO;
import com.tmax.WaplMath.Recommend.model.Curriculum;
import com.tmax.WaplMath.Recommend.model.ProblemDTO;
import com.tmax.WaplMath.Recommend.model.UkMasteryDTO;
import com.tmax.WaplMath.Recommend.model.User;
import com.tmax.WaplMath.Recommend.model.UserExamCardHistory;
import com.tmax.WaplMath.Recommend.model.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.UserKnowledgeKey;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ExamCardProblemRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepository;
import com.tmax.WaplMath.Recommend.repository.TypeUkRepository;
import com.tmax.WaplMath.Recommend.repository.UkRelRepository;
import com.tmax.WaplMath.Recommend.repository.UserExamCardHistoryRepository;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;
import com.tmax.WaplMath.Recommend.repository.UserRepository;


@Service
public class CurriculumCardListService {

	/*
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	// Hyperparameter
	private final Integer MAX_CARD_NUM = 4;
	private final Integer SMALL_CARD_PRE_UK_NUM = 2;
	private final Float PRE_UK_MASTERY_THRESHOLD = 0.6f;
	private final Float SUP_UK_MASTERY_THRESHOLD = 0.6f;
	private final Integer MAX_SMALL_CARD_PROBLEM_NUM = 5;
	private final Integer MAX_EXAM_CARD_PROBLEM_NUM = 20;

	// Repository
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UkRelRepository ukRelRepository;
	@Autowired
	private TypeUkRepository typeUkRepository;
	@Autowired
	private ProblemDemoRepository problemRepository;
	@Autowired
	private CurriculumRepository curriculumRepository;
	@Autowired
	private ProblemDemoRepository problemDemoRepository;
	@Autowired
	private UserKnowledgeRepository userKnowledgeRepository;
	@Autowired
	private ExamCardProblemRepository examCardProblemRepository;
	@Autowired
	private UserExamCardHistoryRepository userExamCardHistoryRepository;

	// Add to USER_EXAM_CURRICULUM_LOG TB
	public Integer addRecommendedCardInfo(String cardId, String cardType, String cardTitle, String userUuid,
			Timestamp recommendedDate, String sectionId, String typeUkUuid, Integer cardSequence) {
		UserExamCardHistory newCard = new UserExamCardHistory(cardId, cardType, cardTitle, userUuid,
				recommendedDate, sectionId, typeUkUuid, cardSequence);
		newCard.setCardSequence(cardSequence);
		userExamCardHistoryRepository.save(newCard);
		return cardSequence + 1;
	}

	// Add to CARD_PROBLEM_MAPPING TB
	public void addCardProblemInfo(CardDTO card) {
		String cardId = card.getCardId();
		List<ProblemDTO> problemList = card.getProblemIdList();
		List<ProblemDTO> cardProbIdList = card.getCardProbIdList();
		for (int i = 0; i < problemList.size(); i++) {
			ProblemDTO problem = problemList.get(i);
			ProblemDTO cardProbId = cardProbIdList.get(i);

			cardProblemMappingRepository.save(new CardProblemMapping(cardProbId.getHigh(), cardId, "high",
					problem.getHigh(), null, new Integer(i + 1)));
			cardProblemMappingRepository.save(new CardProblemMapping(cardProbId.getMiddle(), cardId, "middle",
					problem.getMiddle(), null, new Integer(i + 1)));
			cardProblemMappingRepository.save(new CardProblemMapping(cardProbId.getLow(), cardId, "low",
					problem.getLow(), null, new Integer(i + 1)));
		}
	}

	// Load and generate CardDTO
	public CardDTO loadRecommendedCard(UserExamCurriculumLog targetRecommendedCardLog) {
		CardDTO loadedCard = new CardDTO();
		loadedCard.setCardId(targetRecommendedCardLog.getCardId());
		loadedCard.setCardType(targetRecommendedCardLog.getCardType());
		loadedCard.setCardTitle(targetRecommendedCardLog.getCardTitle());
		List<CardProblemMapping> cardProblemList = cardProblemMappingRepository
				.findAllByCardId(targetRecommendedCardLog.getCardId());

		List<ProblemDTO> problemList = new ArrayList<ProblemDTO>();
		List<ProblemDTO> cardProbIdList = new ArrayList<ProblemDTO>();
		ProblemDTO problem = new ProblemDTO();
		ProblemDTO cardProbId = new ProblemDTO();

		Integer prevProbSequence = 1;
		for (CardProblemMapping cardProblem : cardProblemList) {
			Integer probSequence = cardProblem.getProbSequence();
			if (prevProbSequence != probSequence) {
				prevProbSequence = probSequence;
				problemList.add(problem);
				cardProbIdList.add(cardProbId);
				problem = new ProblemDTO();
				cardProbId = new ProblemDTO();
			}

			String ukUuid = cardProblem.getUkUuid();
			String difficulty = cardProblem.getDifficulty();
			String mappingId = cardProblem.getMappingId();

			switch (difficulty) {
			case "high":
				problem.setHigh(ukUuid);
				cardProbId.setHigh(mappingId);
				break;
			case "middle":
				problem.setMiddle(ukUuid);
				cardProbId.setMiddle(mappingId);
				break;
			case "low":
				problem.setLow(ukUuid);
				cardProbId.setLow(mappingId);
				break;
			}
		}
		problemList.add(problem);
		cardProbIdList.add(cardProbId);

		loadedCard.setProblemIdList(problemList);
		loadedCard.setCardProbIdList(cardProbIdList);

		return loadedCard;
	}

	public CardDTO addProblemList(CardDTO card, List<UkMasteryDTO> ukUuidMasteryList, Integer MAX_PROBLEM_NUM,
			String logLevel) {
		List<ProblemDTO> problemList = card.getProblemIdList();
		List<ProblemDTO> cardProbIdList = card.getCardProbIdList();
		int probCnt = problemList.size();

		while (true) {
			for (UkMasteryDTO ukMastery : ukUuidMasteryList) {
				String ukUuid = ukMastery.getUkId();
				String logMsg;
				switch (logLevel) {
					case "uk": logMsg = String.format("ukUuid %s 문제 추가", ukUuid); break;
					case "mastery": logMsg = String.format("ukUuid %s 추가 (mastery = %s)", ukUuid, ukMastery.getUkMastery()); break;
					case "preMastery": logMsg = String.format("preUkUuid %s 추가 (mastery = %s)", ukUuid, ukMastery.getUkMastery()); break;
					default: logMsg = ""; break;
				}
				logger.info(logMsg);
				problemList.add(new ProblemDTO(ukUuid, ukUuid, ukUuid));
				cardProbIdList.add(new ProblemDTO(UUID.randomUUID().toString().substring(0, 32),
						UUID.randomUUID().toString().substring(0, 32), UUID.randomUUID().toString().substring(0, 32)));

				probCnt += 1;
				if (probCnt == MAX_PROBLEM_NUM)
					break;
			}
			if (probCnt == MAX_PROBLEM_NUM)
				break;
		}
		card.setProblemIdList(problemList);
		card.setCardProbIdList(cardProbIdList);

		return card;
	}

	public CardDTO generateSectionCard(String cardId, String sectionId, String cardTitle) {
		CardDTO sectionCard = new CardDTO();
		sectionCard.setCardId(cardId);
		sectionCard.setCardType("중간평가");
		sectionCard.setCardTitle(cardTitle);
		sectionCard.setProblemIdList(new ArrayList<ProblemDTO>());
		sectionCard.setCardProbIdList(new ArrayList<ProblemDTO>());

		// sectionId 내의 모든 문제의 uk 가져와
		List<String> sectionUkUuidList = problemRepository.findAllUkUuidBySection(sectionId);

		List<UkMasteryDTO> ukUuidMasteryList = new ArrayList<UkMasteryDTO>();
		for (String ukUuid : sectionUkUuidList)
			ukUuidMasteryList.add(new UkMasteryDTO(ukUuid, null));
		sectionCard = addProblemList(sectionCard, ukUuidMasteryList, MAX_EXAM_CARD_PROBLEM_NUM, "uk");
		return sectionCard;
	}

	public CardDTO generateTypeCard(String cardId, String typeUkId, String cardTitle, String userId) throws Exception {
		CardDTO typeCard = new CardDTO();

		typeCard.setCardId(cardId);
		typeCard.setCardType("유형");
		typeCard.setCardTitle(cardTitle);
		typeCard.setProblemIdList(new ArrayList<ProblemDTO>());
		typeCard.setCardProbIdList(new ArrayList<ProblemDTO>());

		List<String> ukUuidList = problemRepository.findAllUkUuidByTypeUkId(typeUkId);
		if (ukUuidList.size() == 0)
			return new CardDTO();

		else {
			List<String> preUkUuidList = ukRelRepository.findPreUkUuidList(ukUuidList, "선수");
			logger.info("ukUuidList: " + ukUuidList.toString());
			logger.info("preukUuid: " + preUkUuidList.toString());

			List<UkMasteryDTO> preUkUuidMasteryList = new ArrayList<UkMasteryDTO>(); // UkMasteryDTO 로 고치기
			for (String preUkUuid : preUkUuidList) {
				UserKnowledge preUserKnowledge;
				try {
					UserKnowledgeKey preMasteryKey = new UserKnowledgeKey();
					preMasteryKey.setUserUuid(userId);
					preMasteryKey.setUkUuid(preUkUuid);
					preUserKnowledge = userKnowledgeRepository.findById(preMasteryKey)
							.orElseThrow(() -> new Exception(preUkUuid));
				} catch (Exception e) {
					logger.info(String.format("User mastery of ukUuid %s not in USER_KNOWLEDGE TB.", e.getMessage()));
					continue;
				}
				Float preUkMastery = preUserKnowledge.getUkMastery();
				if (preUkMastery <= PRE_UK_MASTERY_THRESHOLD)
					preUkUuidMasteryList.add(new UkMasteryDTO(preUkUuid, Float.toString(preUkMastery)));
			}

			// preUk sort by uk mastery
			Collections.sort(preUkUuidMasteryList, new Comparator<UkMasteryDTO>() {
				public int compare(UkMasteryDTO preUk1, UkMasteryDTO preUk2) {
					Float preUk1Mastery = Float.parseFloat(preUk1.getUkMastery());
					Float preUk2Mastery = Float.parseFloat(preUk2.getUkMastery());
					return preUk1Mastery.compareTo(preUk2Mastery);
				}
			});

			typeCard = addProblemList(typeCard, preUkUuidMasteryList, SMALL_CARD_PRE_UK_NUM, "preMastery");

			List<UkMasteryDTO> ukUuidMasteryList = new ArrayList<UkMasteryDTO>();
			for (String ukUuid : ukUuidList)
				ukUuidMasteryList.add(new UkMasteryDTO(ukUuid, null));

			typeCard = addProblemList(typeCard, ukUuidMasteryList, MAX_SMALL_CARD_PROBLEM_NUM, "uk");
			return typeCard;
		}
	}

	public CardDTO generateSupplementCard(String cardId, String cardTitle, String userId,
			List<UserKnowledge> lowMasteryList) {
		CardDTO supplementCard = new CardDTO();

		supplementCard.setCardId(cardId);
		supplementCard.setCardType("보충");
		supplementCard.setCardTitle(cardTitle);
		supplementCard.setProblemIdList(new ArrayList<ProblemDTO>());
		supplementCard.setCardProbIdList(new ArrayList<ProblemDTO>());

		List<UkMasteryDTO> ukUuidMasteryList = new ArrayList<UkMasteryDTO>();
		for (UserKnowledge lowMasteryUK : lowMasteryList) {
			String ukUuid = lowMasteryUK.getUkUuid();
			Float mastery = lowMasteryUK.getUkMastery();
			ukUuidMasteryList.add(new UkMasteryDTO(ukUuid, Float.toString(mastery)));
		}

		supplementCard = addProblemList(supplementCard, ukUuidMasteryList, MAX_SMALL_CARD_PROBLEM_NUM, "mastery");
		return supplementCard;
	}

	public CardDTO generateExamCard(String cardId, String cardTitle, String userId, List<String> chapterList) {
		CardDTO examCard = new CardDTO();

		examCard.setCardId(cardId);
		examCard.setCardType("모의고사");
		examCard.setCardTitle(cardTitle);
		examCard.setProblemIdList(new ArrayList<ProblemDTO>());
		examCard.setCardProbIdList(new ArrayList<ProblemDTO>());

		List<String> examUkIdList = problemDemoRepository.findAllUkUuidByChapterList(chapterList);
		List<UkMasteryDTO> ukUuidMasteryList = new ArrayList<UkMasteryDTO>();
		for (String ukUuid : examUkIdList)
			ukUuidMasteryList.add(new UkMasteryDTO(ukUuid, null));
		examCard = addProblemList(examCard, ukUuidMasteryList, MAX_EXAM_CARD_PROBLEM_NUM, "uk");
		
		return examCard;
	}

	public Map<String, Object> getCurriculumCardList(String userId, String date) throws Exception {
		Map<String, Object> output = new HashMap<String, Object>();

		// 시험 범위 중단원 (데모용)
		List<String> chapterList = new ArrayList<String>(Arrays.asList("중등-중2-1학-03", "중등-중2-1학-04"));

		// parse date and convert to Timestamp type
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		Timestamp targetDate;
		try {
			LocalDateTime currentDateTime = LocalDate.parse(date, dtf).atStartOfDay();
			targetDate = Timestamp.valueOf(currentDateTime);
		} catch (DateTimeParseException e) {
			output.put("resultMessage", "'date' should be in shape of 'yyyy-MM-dd'.");
			return output;
		}

		// Load user information from USER_MASTER TB
		User userInfo;
		try {
			userInfo = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			output.put("resultMessage", String.format("userId = %s is not in USER_MASTER TB.", e.getMessage()));
			return output;
		}

		// Check whether user exam information is null
		String examType = userInfo.getExamType();
		Timestamp examStartDate = userInfo.getExamStartDate();
		Timestamp examDueDate = userInfo.getExamDueDate();
		String currentCurriculumId = userInfo.getCurrentCurriculumId();
		Integer examTargetScore = userInfo.getExamTargetScore();
		if (currentCurriculumId == null || examType == null || examStartDate == null || examDueDate == null
				|| examTargetScore == null) {
			output.put("resultMessage", "One of user's exam infomation is null. Call ExamInfo PUT service first.");
			return output;
		}

		List<CardDTO> cardList = new ArrayList<CardDTO>();

		// 이미 저장되어 있다? 예전 기록 카드 불러오기
		List<UserExamCurriculumLog> targetRecommendedCardLogList = userExamCurriculumLogRepository
				.findAllByRecommendedDate(userId, targetDate);
		if (targetRecommendedCardLogList.size() != 0) {
			logger.info("\n이미 추천 기록이 있어서 불러옴");
			for (UserExamCurriculumLog targetRecommendedCardLog : targetRecommendedCardLogList) {
				CardDTO loadedCard = loadRecommendedCard(targetRecommendedCardLog);
				cardList.add(loadedCard);
			}
			output.put("resultMessage", "Successfully return curriculum card list.");
			output.put("isComletable", "true");
			output.put("cardList", cardList);
			return output;
		}

		// 기록이 없다? 신 카드 만들기
		Integer newCardSequence = userExamCurriculumLogRepository.findLastCardSequenceByUserUuid(userId).orElse(0) + 1;

		// 첫날 이면 중간평가!
		if (targetDate.equals(examStartDate)) {
			String cardType = "중간평가";
			Curriculum firstCurriculum = curriculumRepository.findNearestSection(currentCurriculumId);
			String cardSectionId = firstCurriculum.getCurriculumId();
			String cardTitle = firstCurriculum.getSection();
			logger.info("\n첫날이라 중간평가 진행: " + cardSectionId);

			// 카드 생성
			String cardId = userId + '-' + newCardSequence.toString() + '-' + cardType;
			CardDTO sectionCard = generateSectionCard(cardId, cardSectionId, cardTitle);
			// DB 주입
			newCardSequence = addRecommendedCardInfo(cardId, cardType, cardTitle, userId, targetDate, cardSectionId,
					null, newCardSequence);
			addCardProblemInfo(sectionCard);
			// 결과 추가
			cardList.add(sectionCard);
			output.put("resultMessage", "Successfully return curriculum card list.");
			output.put("isComletable", "true");
			output.put("cardList", cardList);
			return output;
		}

		// 첫날이 아니면? 유형/보충/모의고사 카드
		List<String> totalSectionList = typeUkRepository.findAllSection(chapterList);
		Set<String> totalSectionSet = new HashSet<String>(totalSectionList); // 토탈 범위 단원들
		logger.info("1. 시험범위 전체 단원들 : " + totalSectionSet.toString());

		List<String> userCompletedTypeUkList = userExamCurriculumLogRepository.findAllTypeUkUuid(userId); // 이미 푼 typeUK
		logger.info("2. 유저가 푼 유형 UK들 : " + userCompletedTypeUkList.toString());

		List<String> notCompletedSectionList;
		if (userCompletedTypeUkList.size() == 0) {
			logger.info("- 푼게 하나도 없음.");
			notCompletedSectionList = typeUkRepository.findAllSection(chapterList);
		} else
			notCompletedSectionList = typeUkRepository.findAllSectionNotInList(chapterList, userCompletedTypeUkList);
		Set<String> notCompletedSectionSet = new HashSet<String>(notCompletedSectionList); // 안푼 단원들
		logger.info("3. 안푼 단원들 : " + notCompletedSectionSet.toString());

		totalSectionSet.removeAll(notCompletedSectionSet); // totalSectionSet에 완벽히 푼 단원들 저장됨
		logger.info("4. 완벽히 유형카드를 푼 단원들 : " + totalSectionSet.toString());

		List<String> completedSectionList = userExamCurriculumLogRepository.findAllSectionId(userId);
		Set<String> completedSectionSet = new HashSet<String>(completedSectionList);
		totalSectionSet.removeAll(completedSectionSet); // totalSectionSet에 완벽히 푼 단원들 - 이미 푼 중간평가 저장됨
		logger.info("5. 이미 중간평가 카드 푼 단원들 : " + completedSectionSet.toString());
		logger.info("6. 중간평가 대상인 최종 단원 : " + totalSectionSet.toString());

		// 완벽히 푼 단원이 있다? 중간평가 할차례!
		if (totalSectionSet.size() != 0) {
			String cardType = "중간평가";
			String cardSectionId = totalSectionSet.iterator().next();
			String cardTitle = curriculumRepository.findSectionNameById(cardSectionId);
			logger.info("\n중간에 다 풀었으니까 중간평가 진행: " + cardSectionId);

			// 카드 생성
			String cardId = userId + '-' + newCardSequence.toString() + '-' + cardType;
			CardDTO sectionCard = generateSectionCard(cardId, cardSectionId, cardTitle);
			// DB 주입
			newCardSequence = addRecommendedCardInfo(cardId, cardType, cardTitle, userId, targetDate, cardSectionId,
					null, newCardSequence);
			addCardProblemInfo(sectionCard);
			// 결과 추가
			cardList.add(sectionCard);
			output.put("resultMessage", "Successfully return curriculum card list.");
			output.put("isComletable", "true");
			output.put("cardList", cardList);
			return output;
		}

		// 유형 UK (혹은 보충 UK) 나갈차례!
		// 보충 필요한지 판단
		Integer lastSupplementCardSequence = userExamCurriculumLogRepository.findLastSupCardSequence(userId).orElse(1);
		List<String> supplementCardUkList = new ArrayList<String>(Arrays.asList(""));
		if (lastSupplementCardSequence != 1)
			supplementCardUkList = userExamCurriculumLogRepository.findAllSupCardUkUuid(userId);
		logger.info("\n7. 지금까지 보충 카드로 추천된 ukID 리스트 = ");
		logger.info(supplementCardUkList.toString());
		List<UserKnowledge> lowMasteryList = userExamCurriculumLogRepository.findAllLowMasteryUkUuid(userId,
				lastSupplementCardSequence, supplementCardUkList, SUP_UK_MASTERY_THRESHOLD);

		logger.info("8. 보충 카드 제외, 유형 카드로 추천된 UK들의 이해도 리스트 = ");
		for (UserKnowledge lowMastery : lowMasteryList)
			logger.info(
					String.format("   ukId = %s, mastery = %f", lowMastery.getUkUuid(), lowMastery.getUkMastery()));

		// 보충 채울만큼 사이즈가 나온다? 보충 카드
		if (lowMasteryList.size() >= MAX_SMALL_CARD_PROBLEM_NUM) {
			String cardType = "보충";
			String cardTitle = "";
			logger.info("\n이해도 낮은게 많아서 보충 카드 진행");

			String cardId = userId + '-' + newCardSequence.toString() + '-' + cardType;
			CardDTO supplementCard;
			supplementCard = generateSupplementCard(cardId, cardTitle, userId, lowMasteryList);

			newCardSequence = addRecommendedCardInfo(cardId, cardType, cardTitle, userId, targetDate, null, null,
					newCardSequence);
			addCardProblemInfo(supplementCard);

			cardList.add(supplementCard);
		}

		// 나머지 카드들은? 유형카드로 채우기
		List<String> remainTypeUkIdList;
		if (userCompletedTypeUkList.size() == 0)
			remainTypeUkIdList = typeUkRepository.findTypeUkIdList(chapterList);
		else
			remainTypeUkIdList = typeUkRepository.findNotCompletedTypeUkIdList(chapterList, userCompletedTypeUkList);

		// 공부 안한 유형uk가 남아있다? 유형카드
		if (remainTypeUkIdList.size() != 0) {
			for (String typeUkId : remainTypeUkIdList) {
				String cardType = "유형";
				String cardTitle = typeUkRepository.findTypeUkNameById(typeUkId);
				logger.info("\n중간평가 아니니까 유형 UK 카드 진행: " + typeUkId);

				String cardId = userId + '-' + newCardSequence.toString() + '-' + cardType;
				CardDTO typeCard;
				try {
					typeCard = generateTypeCard(cardId, typeUkId, cardTitle, userId);
				} catch (Exception e) {
					output.put("resultMessage", e.getMessage());
					return output;
				}

				// 문제가 하나도 없으면 뛰어넘자
				if (typeCard == new CardDTO())
					continue;

				newCardSequence = addRecommendedCardInfo(cardId, cardType, cardTitle, userId, targetDate, null,
						typeUkId, newCardSequence);
				addCardProblemInfo(typeCard);
				// 결과 추가
				cardList.add(typeCard);
				if (cardList.size() == MAX_CARD_NUM)
					break;
			}
			output.put("resultMessage", "Successfully return curriculum card list.");
			output.put("isComletable", "true");
			output.put("cardList", cardList);
			return output;
		}

		// 유형 uk가 안남아있다? 보충 카드는 있는데? 보충카드 ㄱㄱ
		if (cardList.size() != 0) {
			output.put("resultMessage", "Successfully return curriculum card list.");
			output.put("isComletable", "true");
			output.put("cardList", cardList);
			return output;
		} else {
			String cardType = "모의고사";
			String cardTitle = "";
			logger.info("\n다풀었다. 모의고사 카드 진행. ");

			String cardId = userId + '-' + newCardSequence.toString() + '-' + cardType;
			CardDTO examCard = generateExamCard(cardId, cardTitle, userId, chapterList);

			newCardSequence = addRecommendedCardInfo(cardId, cardType, cardTitle, userId, targetDate, null, null,
					newCardSequence);
			addCardProblemInfo(examCard);
			cardList.add(examCard);

			output.put("resultMessage", "Successfully return curriculum card list.");
			output.put("isComletable", "true");
			output.put("cardList", cardList);
			return output;
		}
	}
	 */
}
