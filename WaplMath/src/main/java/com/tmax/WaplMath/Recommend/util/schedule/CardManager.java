package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.tmax.WaplMath.Recommend.dto.CardDTO;
import com.tmax.WaplMath.Recommend.dto.DiffProbListDTO;
import com.tmax.WaplMath.Recommend.dto.ProblemSetDTO;
import com.tmax.WaplMath.Recommend.dto.SectionMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.SubSectionMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;

@Component
public class CardManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// Hyperparameter
	private static final Integer MAX_SMALL_CARD_PROBLEM_NUM = 4;
	private static final Integer MAX_EXAM_CARD_PROBLEM_NUM = 15;
	private static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;

	// Constant
	private static final String TYPE_CARD_TYPE = "type";
	private static final String SUPPLEMENT_CARD_TYPE = "supple";
	private static final String MID_EXAM_CARD_TYPE = "midExam";
	private static final String TRIAL_EXAM_CARD_TYPE = "trialExam";
	private static final String SUPPLEMENT_CARD_TITLE_FORMAT = "취약 유형 %d개 복습";

	@Autowired
	private ProblemRepo problemRepo;
	@Autowired
	private ProblemTypeRepo problemTypeRepo;
	@Autowired
	private CurriculumRepository curriculumRepo;
	@Autowired
	private UserKnowledgeRepository userKnowledgeRepo;

	public String userId;
	public Set<Integer> solvedProbIdSet;

	public void setSolvedProbIdSet(List<Integer> solvedProbIdList) {
		this.solvedProbIdSet = new HashSet<Integer>();
		this.solvedProbIdSet.addAll(solvedProbIdList);
	}

	public DiffProbListDTO padProbList(DiffProbListDTO diffProbList) {
		List<Problem> highProbList, middleProbList, lowProbList;
		List<Problem> maxProbList = new ArrayList<Problem>();

		middleProbList = diffProbList.getMiddleProbList();
		highProbList = diffProbList.getHighProbList();
		lowProbList = diffProbList.getLowProbList();

		if (middleProbList.size() >= highProbList.size()) {
			if (middleProbList.size() >= lowProbList.size())
				maxProbList.addAll(middleProbList);
			else
				maxProbList.addAll(lowProbList);
		} else {
			if (highProbList.size() >= lowProbList.size())
				maxProbList.addAll(highProbList);
			else
				maxProbList.addAll(lowProbList);
		}

		Integer maxLength = maxProbList.size();
		middleProbList.addAll(maxProbList.subList(middleProbList.size(), maxLength));
		highProbList.addAll(maxProbList.subList(highProbList.size(), maxLength));
		lowProbList.addAll(maxProbList.subList(lowProbList.size(), maxLength));

		diffProbList.setMiddleProbList(middleProbList);
		diffProbList.setHighProbList(highProbList);
		diffProbList.setLowProbList(lowProbList);
		return diffProbList;
	}

	public DiffProbListDTO generateDiffProbList(List<Problem> probList) {
		DiffProbListDTO diffProbList = new DiffProbListDTO();
		for (Problem prob : probList) {
			String difficulty = prob.getDifficulty();
			diffProbList.addDiffProb(prob, difficulty);
		}
		return diffProbList;
	}

	public void printDiffProbList(DiffProbListDTO diffProbList) {
		for (String difficulty : Arrays.asList("상", "중", "하")) {
			List<Problem> probList = diffProbList.getDiffProbList(difficulty);
			List<Integer> probIdList = new ArrayList<Integer>();
			probList.forEach(prob -> probIdList.add(prob.getProbId()));
			logger.info(String.format("	%s 난이도 문제들 = " + probIdList.toString(), difficulty));
		}
		logger.info("");
	}

	public CardDTO addProblemList(CardDTO card, DiffProbListDTO diffProbList, Integer PROBLEM_NUM) {
		List<ProblemSetDTO> problemSetList = card.getProbIdSetList();
		Integer estimatedTime = card.getEstimatedTime();
		List<Problem> highProbList, middleProbList, lowProbList;

		DiffProbListDTO paddedProbList = padProbList(diffProbList);
		//		logger.info("	문제 패딩 이후");
		//		printDiffProbList(paddedProbList);

		middleProbList = paddedProbList.getMiddleProbList();
		highProbList = paddedProbList.getHighProbList();
		lowProbList = paddedProbList.getLowProbList();

		int probSetCnt = 0;
		Integer timeTotal = estimatedTime;
		while (true) {
			Problem highProb = highProbList.get(probSetCnt % highProbList.size());
			Problem middleProb = middleProbList.get(probSetCnt % middleProbList.size());
			Problem lowProb = lowProbList.get(probSetCnt % lowProbList.size());

			// add problem set
			problemSetList.add(new ProblemSetDTO(highProb.getProbId(), middleProb.getProbId(), lowProb.getProbId()));
			this.solvedProbIdSet.add(highProb.getProbId());
			this.solvedProbIdSet.add(middleProb.getProbId());
			this.solvedProbIdSet.add(lowProb.getProbId());

			// calc estimated time
			Float highProbTime = highProb.getTimeRecommendation();
			Integer highEstimateTime = (highProbTime == null || highProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(highProbTime);
			Float middleProbTime = middleProb.getTimeRecommendation();
			Integer middleEstimateTime = (middleProbTime == null || middleProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME
				: Math.round(middleProbTime);
			Float lowProbTime = lowProb.getTimeRecommendation();
			Integer lowEstimateTime = (lowProbTime == null || lowProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(lowProbTime);
			timeTotal += Math.round((highEstimateTime + middleEstimateTime + lowEstimateTime) / 3);

			probSetCnt += 1;
			if (probSetCnt == PROBLEM_NUM)
				break;
		}
		card.setEstimatedTime(timeTotal);
		card.setProbIdSetList(problemSetList);
		return card;
	}

	public CardDTO addSubSectionProblem(CardDTO card, String subSectionId, Integer probNum, Integer verbose) {
		List<Integer> typeIdList = problemTypeRepo.findTypeIdListInSubSection(subSectionId);
		List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findTypeMasteryList(userId, typeIdList);
		Map<Integer, Integer> typeProbNumMap = new HashMap<Integer, Integer>();

		if (verbose != 0) {
			logger.info("소단원 {} 내 유형들 = ", subSectionId);
			for (TypeMasteryDTO typeMastery : typeMasteryList)
				logger.info("{} (mastery={})", typeMastery.getTypeId(), typeMastery.getMastery());
		}

		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		while (probCnt != probNum) {
			for (TypeMasteryDTO typeMastery : typeMasteryList) {
				Integer typeId = typeMastery.getTypeId();
				Float mastery = typeMastery.getMastery();
				if (problemRepo.NfindProbListByType(typeId, solvedProbIdSet).size() != 0) {
					if (typeProbNumMap.containsKey(typeId))
						typeProbNumMap.put(typeId, typeProbNumMap.get(typeId) + 1);
					else
						typeProbNumMap.put(typeId, 1);
					cardDetailJson.addProperty(typeId.toString(), mastery * 100.0f);
					probCnt += 1;
				}
				if (probCnt == probNum)
					break;
			}
		}

		for (Integer typeId : typeProbNumMap.keySet()) {
			Integer typeProbNum = typeProbNumMap.get(typeId);
			if (verbose != 0)
				logger.info("	유형 {} 내에서 {} 문제 출제", typeId, typeProbNum);
			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			card = addProblemList(card, diffProbList, typeProbNum);
		}
		card.setCardDetail(cardDetailJson.toString());
		return card;
	}

	public CardDTO generateTypeCard(Integer typeId) {
		CardDTO typeCard = new CardDTO();
		String cardTitle = problemTypeRepo.findTypeNameById(typeId);

		typeCard.setCardType(TYPE_CARD_TYPE);
		typeCard.setCardTitle(cardTitle);
		typeCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		typeCard.setEstimatedTime(0);

		// 유형 점수
		TypeMasteryDTO typeMastery = userKnowledgeRepo.findTypeMastery(userId, typeId);
		typeCard.setCardScore(typeMastery.getMastery() * 100);

		// 유형 카드 상세 정보
		JsonObject cardDetailJson = new JsonObject();
		Curriculum typeCurriculum = curriculumRepo.findByType(typeId);
		cardDetailJson.addProperty("subSection", typeCurriculum.getSubSection());
		cardDetailJson.addProperty("section", typeCurriculum.getSection());
		cardDetailJson.addProperty("chapter", typeCurriculum.getChapter());
		typeCard.setCardDetail(cardDetailJson.toString());

		List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
		if (typeProbList.size() == 0)
			return new CardDTO();
		else {
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			typeCard = addProblemList(typeCard, diffProbList, MAX_SMALL_CARD_PROBLEM_NUM);
			typeCard.setFirstProbLevel("middle");
			return typeCard;
		}
	}

	public CardDTO generateSupplementCard(List<TypeMasteryDTO> lowMasteryTypeList) {
		CardDTO supplementCard = new CardDTO();
		String cardTitle = String.format(SUPPLEMENT_CARD_TITLE_FORMAT, lowMasteryTypeList.size());

		supplementCard.setCardType(SUPPLEMENT_CARD_TYPE);
		supplementCard.setCardTitle(cardTitle);
		supplementCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		supplementCard.setEstimatedTime(0);

		JsonObject cardDetailJson = new JsonObject();
		int cnt = 1;
		for (TypeMasteryDTO typeMastery : lowMasteryTypeList) {
			Integer typeId = typeMastery.getTypeId();
			String typeName = problemTypeRepo.NfindTypeNameById(typeId);
			Float mastery = typeMastery.getMastery();
			logger.info("보충카드 {}번째 유형 = {} (mastery={}) {} 문제", cnt, typeId, mastery, cnt == 1 ? 2 : 1);

			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, null);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			supplementCard = addProblemList(supplementCard, diffProbList, cnt == 1 ? 2 : 1);

			cardDetailJson.addProperty(typeName, mastery * 100.0f);
			cnt += 1;
		}
		supplementCard.setCardDetail(cardDetailJson.toString());
		supplementCard.setFirstProbLevel("low");
		return supplementCard;
	}

	public CardDTO generateMidExamCard(String sectionId) {
		CardDTO midExamCard = new CardDTO();
		String cardTitle = curriculumRepo.findSectionName(sectionId);

		midExamCard.setCardType(MID_EXAM_CARD_TYPE);
		midExamCard.setCardTitle(cardTitle);
		midExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		midExamCard.setEstimatedTime(0);

		// 중단원 점수
		SectionMasteryDTO sectionMastery = userKnowledgeRepo.findSectionMastery(userId, sectionId);
		midExamCard.setCardScore(sectionMastery.getMastery() * 100);

		// 중단원 내의 모든 소단원 불러오기
		List<String> subSectionIdList = curriculumRepo.findSubSectionListInSection(sectionId);
		List<SubSectionMasteryDTO> subSectionMasteryList = userKnowledgeRepo.findSubSectionMasteryList(userId, subSectionIdList);
		Map<String, Integer> subSectionProbNumMap = new HashMap<String, Integer>();
		logger.info("중단원 {} 내 소단원들 = ", sectionId);
		for (SubSectionMasteryDTO subSectionMastery : subSectionMasteryList)
			logger.info("{} (mastery={})", subSectionMastery.getSubSectionId(), subSectionMastery.getMastery());

		JsonObject cardDetailJson = new JsonObject();
		int idx = 0;
		while (idx != MAX_EXAM_CARD_PROBLEM_NUM) {
			SubSectionMasteryDTO subSectionMastery = subSectionMasteryList.get(idx % subSectionMasteryList.size());
			String subSectionId = subSectionMastery.getSubSectionId();
			String subSectionName = curriculumRepo.findSubSectionName(subSectionId);
			Float mastery = subSectionMastery.getMastery();
			if (subSectionProbNumMap.containsKey(subSectionId))
				subSectionProbNumMap.put(subSectionId, subSectionProbNumMap.get(subSectionId) + 1);
			else
				subSectionProbNumMap.put(subSectionId, 1);
			cardDetailJson.addProperty(subSectionName, mastery * 100.0f);
			idx += 1;
		}

		for (String subSectionId : subSectionIdList) {
			Integer probNum = subSectionProbNumMap.get(subSectionId);
			logger.info("소단원 {} 내에서 {} 문제 출제", subSectionId, probNum);
			midExamCard = addSubSectionProblem(midExamCard, subSectionId, probNum, 1);
		}
		midExamCard.setCardDetail(cardDetailJson.toString());
		midExamCard.setFirstProbLevel("middle");
		return midExamCard;
	}

	public CardDTO generateTrialExamCard(List<String> subSectionList) {
		CardDTO trialExamCard = new CardDTO();
		String cardTitle = "";

		trialExamCard.setCardType(TRIAL_EXAM_CARD_TYPE);
		trialExamCard.setCardTitle(cardTitle);
		trialExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		trialExamCard.setEstimatedTime(0);

		List<Problem> trialExamProbList;
		trialExamProbList = problemRepo.findProbListInSubSectionList(subSectionList, solvedProbIdSet);
		DiffProbListDTO diffProbList = generateDiffProbList(trialExamProbList);
		trialExamCard = addProblemList(trialExamCard, diffProbList, MAX_EXAM_CARD_PROBLEM_NUM);
		return trialExamCard;
	}

}
