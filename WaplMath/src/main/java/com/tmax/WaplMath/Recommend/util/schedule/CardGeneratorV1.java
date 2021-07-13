package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.tmax.WaplMath.Recommend.dto.mastery.CurrMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV1;
import com.tmax.WaplMath.Recommend.dto.schedule.DiffProbListDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ProblemSetDTO;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Generate normal/exam schedule card information v1
 * @author Sangheon_lee
 */
@Slf4j
@Component
public class CardGeneratorV1 {

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
	private static final String TRIAL_EXAM_CARD_TITLE_FORMAT = "중학교 %s학년 %s학기 %s 대비";
	
	// logging option
	private final Boolean printProbInfo = false; //level 1
	private final Boolean printTypeInfo = false; //level 2
	private final Boolean printCurrInfo = false; //level 2

	@Autowired
	private ProblemRepo problemRepo;
	@Autowired
	private ProblemTypeRepo problemTypeRepo;
	@Autowired
	private CurriculumRepository curriculumRepo;
	@Autowired
	private UserKnowledgeRepository userKnowledgeRepo;

	public String userId;
	public @Setter Set<Integer> solvedProbIdSet;
	public @Setter Set<String> examSubSectionIdSet;

	// 문제 난이도 별 문제 Id 모아놓는 모듈.
	public DiffProbListDTO generateDiffProbList(List<Problem> probList) {
		DiffProbListDTO diffProbList = new DiffProbListDTO();
		for (Problem prob : probList) {
			String difficulty = prob.getDifficulty();
			diffProbList.addDiffProb(prob, difficulty);
		}
		return diffProbList;
	}

	public void printDiffProbList(DiffProbListDTO diffProbList) {
		if (printProbInfo) {
			for (String difficulty : Arrays.asList("상", "중", "하")) {
				List<Problem> probList = diffProbList.getDiffProbList(difficulty);
				List<Integer> probIdList = new ArrayList<Integer>();
				probList.forEach(prob -> probIdList.add(prob.getProbId()));
				log.info(String.format("	%s 난이도 문제들 = " + probIdList.toString(), difficulty));
			}
			log.info("");
		}
	}

	// 난이도의 문제가 적을 때, 뒤를 제일 긴 문제 list 문제들로 패딩
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

	// 문제 풀이 예상 시간 측정
	public Integer getEstimatedTime(Problem prob) {
		Float probTime = prob.getTimeRecommendation();
		return (probTime == null || probTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(probTime);
	}

	// 카드 안에 난이도 별로 문제 담기
	public CardDTOV1 addProblemList(CardDTOV1 card, DiffProbListDTO diffProbList, Integer PROBLEM_NUM) {
		List<ProblemSetDTO> problemSetList = card.getProbIdSetList();
		Integer estimatedTime = card.getEstimatedTime();
		DiffProbListDTO paddedProbList = padProbList(diffProbList);
		//		log.info("	문제 패딩 이후");
		//		printDiffProbList(paddedProbList);
		List<Problem> highProbList, middleProbList, lowProbList;
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
			Integer highEstimateTime = getEstimatedTime(highProb);
			Integer middleEstimateTime = getEstimatedTime(middleProb);
			Integer lowEstimateTime = getEstimatedTime(lowProb);
			timeTotal += Math.round((highEstimateTime + middleEstimateTime + lowEstimateTime) / 3);

			probSetCnt += 1;
			if (probSetCnt == PROBLEM_NUM)
				break;
		}
		card.setEstimatedTime(timeTotal);
		card.setProbIdSetList(problemSetList);
		return card;
	}

	// 시험/대/중단원 (superCurr) 내에 대/중/소단원 (curr) 별 난이도 고려하여 문제 추출 모듈
	public CardDTOV1 addCurrProblemWithMastery(CardDTOV1 card, String superCurrId, String superCurrType, Integer probNum, Integer verbose) {
		// superCurr 내의 모든 curr 불러오기
		List<String> currIdList = new ArrayList<String>();
		List<CurrMasteryDTO> currMasteryList = new ArrayList<CurrMasteryDTO>();
		Map<String, Integer> currProbNumMap = new HashMap<String, Integer>();

		switch (superCurrType) {
		case "section":
			currIdList = curriculumRepo.findSubSectionListInSection(superCurrId);
			currMasteryList = userKnowledgeRepo.findSubSectionMasteryList(userId, currIdList);
			break;
		case "chapter":
			currIdList = curriculumRepo.findSectionListInChapter(superCurrId);
			currMasteryList = userKnowledgeRepo.findSectionMasteryList(userId, currIdList);
			break;
		case "exam":
			currIdList = curriculumRepo.findChapterListInSubSectionSet(examSubSectionIdSet);
			currMasteryList = userKnowledgeRepo.findChapterMasteryList(userId, currIdList);
		}

		if (printCurrInfo && verbose != 0) {
			log.info("{} {} 내 단원 들 = ", superCurrType, superCurrId);
			for (CurrMasteryDTO currMastery : currMasteryList)
				log.info("{}\t{}\t(mastery={})", currMastery.getCurrId(), currMastery.getCurrName(), currMastery.getMastery());
		}
		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		while (probCnt != probNum) {
			for (CurrMasteryDTO currMastery : currMasteryList) {
				String currId = currMastery.getCurrId();
				String currName = currMastery.getCurrName();
				Float mastery = currMastery.getMastery();
				List<Problem> currProblems = new ArrayList<Problem>();
				switch (superCurrType) {
				case "section":
					currProblems = problemRepo.NfindProbListBySubSection(currId, solvedProbIdSet);
					break;
				case "chapter":
					currProblems = problemRepo.NfindProbListBySection(currId, solvedProbIdSet);
					break;
				case "exam":
					currProblems = problemRepo.NfindProbListByChapter(currId, solvedProbIdSet);
					break;
				}
				if (currProblems.size() != 0) {
					if (currProbNumMap.containsKey(currId))
						currProbNumMap.put(currId, currProbNumMap.get(currId) + 1);
					else
						currProbNumMap.put(currId, 1);
					cardDetailJson.addProperty(currName, mastery * 100.0f);
					probCnt += 1;
				}
				if (probCnt == probNum)
					break;
			}
		}
		for (String currId : currProbNumMap.keySet()) {
			Integer currProbNum = currProbNumMap.get(currId);
			switch (superCurrType) {
			case "section":
				if (printCurrInfo && verbose != 0)
					log.info("소단원 {} 내에서 {} 문제 출제", currId, currProbNum);
				card = addSubSectionProblem(card, currId, currProbNum, verbose);
				break;
			case "chapter":
				if (printCurrInfo && verbose != 0) {
					log.info("");
					log.info("중단원 {} 내에서 {} 문제 출제", currId, currProbNum);
				}
				card = addCurrProblemWithMastery(card, currId, "section", currProbNum, verbose);
				break;
			case "exam":
				if (printCurrInfo && verbose != 0) {
					log.info("");
					log.info("대단원 {} 내에서 {} 문제 출제", currId, currProbNum);
				}
				card = addCurrProblemWithMastery(card, currId, "chapter", currProbNum, verbose);
				break;
			}
		}
		card.setCardDetail(cardDetailJson.toString());
		return card;
	}


	// 소단원 내 문제 출제 모듈. 마스터리가 낮은 유형 순서대로 많이 출제
	public CardDTOV1 addSubSectionProblem(CardDTOV1 card, String subSectionId, Integer probNum, Integer verbose) {
		List<Integer> typeIdList = problemTypeRepo.findTypeIdListInSubSection(subSectionId);
		List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findTypeMasteryList(userId, typeIdList);
		Map<Integer, Integer> typeProbNumMap = new HashMap<Integer, Integer>();
		if (printTypeInfo && verbose != 0) {
			log.info("소단원 {} 내 유형들 = ", subSectionId);
			for (TypeMasteryDTO typeMastery : typeMasteryList)
				log.info("{} (mastery={})", typeMastery.getTypeId(), typeMastery.getMastery());
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
			if (printTypeInfo && verbose != 0)
				log.info("	유형 {} 내에서 {} 문제 출제", typeId, typeProbNum);
			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			card = addProblemList(card, diffProbList, typeProbNum);
		}
		card.setCardDetail(cardDetailJson.toString());
		return card;
	}

	// 실력 향상 / 시험 대비 - 중간 평가 카드 (type=section/chapter)
	public CardDTOV1 generateMidExamCard(String curriculumId, String type) {
		CardDTOV1 midExamCard = new CardDTOV1();
		CurrMasteryDTO mastery;
		if (type.equals("section"))
			mastery = userKnowledgeRepo.findSectionMastery(userId, curriculumId);
		else
			mastery = userKnowledgeRepo.findChapterMastery(userId, curriculumId);
		log.info("{}, {}, {}", mastery.getCurrId(), mastery.getCurrName(), mastery.getMastery());
		midExamCard.setCardType(MID_EXAM_CARD_TYPE);
		midExamCard.setCardTitle(mastery.getCurrName());
		midExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		midExamCard.setEstimatedTime(0);
		// 중단원 점수
		midExamCard.setCardScore(mastery.getMastery() * 100);
		// 문제 넣기
		midExamCard = addCurrProblemWithMastery(midExamCard, curriculumId, type, MAX_EXAM_CARD_PROBLEM_NUM, 1);
		// 첫 문제 레벨
		midExamCard.setFirstProbLevel("middle");
		return midExamCard;
	}

	// 실력 향상 - 유형카드
	public CardDTOV1 generateTypeCard(Integer typeId) {
		CardDTOV1 typeCard = new CardDTOV1();
		String cardTitle = problemTypeRepo.findTypeNameById(typeId);

		typeCard.setCardType(TYPE_CARD_TYPE);
		typeCard.setCardTitle(cardTitle);
		typeCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		typeCard.setEstimatedTime(0);

		// 유형 점수
		TypeMasteryDTO typeMastery = userKnowledgeRepo.findTypeMastery(userId, typeId);
		if (typeMastery != null)
			typeCard.setCardScore(typeMastery.getMastery() * 100);
		else
			typeCard.setCardScore(79.283f);

		// 유형 카드 상세 정보
		JsonObject cardDetailJson = new JsonObject();
		Curriculum typeCurriculum = curriculumRepo.findByType(typeId);
		cardDetailJson.addProperty("subSection", typeCurriculum.getSubSection());
		cardDetailJson.addProperty("section", typeCurriculum.getSection());
		cardDetailJson.addProperty("chapter", typeCurriculum.getChapter());
		typeCard.setCardDetail(cardDetailJson.toString());

		List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
		if (typeProbList.size() == 0)
			return new CardDTOV1();
		else {
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			typeCard = addProblemList(typeCard, diffProbList, MAX_SMALL_CARD_PROBLEM_NUM);
			typeCard.setFirstProbLevel("middle");
			return typeCard;
		}
	}

	// 실력 향상 - 보충카드
	public CardDTOV1 generateSupplementCard(List<TypeMasteryDTO> lowMasteryTypeList) {
		CardDTOV1 supplementCard = new CardDTOV1();
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
			log.info("보충카드 {}번째 유형 = {} (mastery={}) {} 문제", cnt, typeId, mastery, cnt == 1 ? 2 : 1); // 첫 유형은 두 문제

			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, null);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			supplementCard = addProblemList(supplementCard, diffProbList, cnt == 1 ? 2 : 1); // 첫 유형은 두 문제

			cardDetailJson.addProperty(typeName, mastery * 100.0f);
			cnt += 1;
		}
		supplementCard.setCardDetail(cardDetailJson.toString());
		supplementCard.setFirstProbLevel("low");
		return supplementCard;
	}

	// 시험 대비 - 모의고사 카드
	public CardDTOV1 generateTrialExamCard(String trialExamType) {
		CardDTOV1 trialExamCard = new CardDTOV1();
		String cardTitle = "";
		String[] trialExamInfo = trialExamType.split("-");
		if (trialExamInfo[2].equals("mid"))
			cardTitle = String.format(TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0], trialExamInfo[1], "중간고사");
		else if (trialExamInfo[2].equals("final"))
			cardTitle = String.format(TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0], trialExamInfo[1], "기말고사");

		CurrMasteryDTO mastery = userKnowledgeRepo.findExamMastery(userId, examSubSectionIdSet);

		trialExamCard.setCardType(TRIAL_EXAM_CARD_TYPE);
		trialExamCard.setCardTitle(cardTitle);
		trialExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		trialExamCard.setEstimatedTime(0);
		trialExamCard.setCardScore(mastery.getMastery() * 100);
		// 문제 넣기
		trialExamCard = addCurrProblemWithMastery(trialExamCard, "모의고사", "exam", MAX_EXAM_CARD_PROBLEM_NUM, 1);
		// 첫 문제 레벨
		trialExamCard.setFirstProbLevel("middle");
		return trialExamCard;
	}

	public CardDTOV1 generateCard(CardConfigDTO cardConfig) {
		CardDTOV1 card = new CardDTOV1();
		switch (cardConfig.getCardType()) {
		case "type":
			log.info("------ {} card (type {})", cardConfig.getCardType(), cardConfig.getTypeId());
			card = generateTypeCard(cardConfig.getTypeId());
			break;
		case "supple":
			log.info("------ {} card", cardConfig.getCardType());
			card = generateSupplementCard(cardConfig.getTypeMasteryList());
			break;
		case "midExam":
			log.info("------ {} card ({} {})", cardConfig.getCardType(), cardConfig.getTestType(), cardConfig.getCurriculumId());
			card = generateMidExamCard(cardConfig.getCurriculumId(), cardConfig.getTestType());
			break;
		case "trialExam":
			log.info("------ {} card ({})", cardConfig.getCardType(), cardConfig.getExamKeyword());
			card = generateTrialExamCard(cardConfig.getExamKeyword());
			break;
		}
		return card;
	}

}
