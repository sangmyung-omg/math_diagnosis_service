package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.tmax.WaplMath.Recommend.dto.mastery.CurrMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTOV2;
import com.tmax.WaplMath.Recommend.dto.schedule.DiffProbListDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ProblemSetListDTO;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;

import lombok.Setter;

/**
 * Generate normal/exam schedule card information V2
 * @author Sangheon_lee
 * @since 2021-06-30
 */

@Component
public class CardGeneratorV2 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// Hyperparameter
	private static final Integer MIN_TYPE_CARD_PROB_NUM = 2;
	private static final Integer MAX_TYPE_CARD_PROB_NUM = 5;
	private static final Integer SUPPLE_CARD_PROB_NUM_PER_TYPE = 2;
	private static final Integer MID_EXAM_CARD_PROB_NUM = 20;
	private static final Integer MID_EXAM_CARD_HIGH_PROB = 5;
	private static final Integer MID_EXAM_CARD_MIDDLE_PROB = 9;
	private static final Integer MID_EXAM_CARD_LOW_PROB = 6;

	private static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;
	private static final float MASTERY_HIGH_THRESHOLD = 0.7f;
	private static final float MASTERY_LOW_THRESHOLD = 0.4f;

	// Constant
	private static final String TYPE_CARD_TYPE = "type";
	private static final String SUPPLEMENT_CARD_TYPE = "supple";
	private static final String SECTION_MID_EXAM_CARD_TYPE = "sectionMidExam";
	private static final String CHAPTER_MID_EXAM_CARD_TYPE = "chapterMidExam";
	private static final String TRIAL_EXAM_CARD_TYPE = "trialExam";
	private static final String SUPPLEMENT_CARD_TITLE_FORMAT = "취약 유형 %d개 복습";
	private static final String TRIAL_EXAM_CARD_TITLE_FORMAT = "중학교 %s학년 %s학기 %s 대비";

	// logging option
	private final Boolean printProbInfo = true; //level 1
	private final Boolean printMastery = false; //level 2
	private final Boolean printTypeInfo = true; //level 3
	private final Boolean printCurrInfo = true; //level 4

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

	public String getFirstProbLevel(Float mastery) {
		if (mastery >= MASTERY_HIGH_THRESHOLD)
			return "high";
		else if (mastery < MASTERY_HIGH_THRESHOLD && mastery >= MASTERY_LOW_THRESHOLD)
			return "middle";
		else
			return "low";
	}

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
				logger.info(String.format("	%s 난이도 문제들 = " + probIdList.toString(), difficulty));
			}
			logger.info("");
		}
	}

	// 문제 리스트를 최대 특정 크기만큼 자름
	public List<Problem> sliceProbList(List<Problem> probList, Integer MAx_PROBLEM_NUM) {
		return probList.subList(0, Math.min(MAx_PROBLEM_NUM, probList.size()));
	}

	// Problem 객체 리스트에서 Integer 리스트 추출
	public List<Integer> getIdListFromProbList(List<Problem> probList) {
		List<Integer> probIdList = new ArrayList<Integer>();
		probList.forEach(e -> probIdList.add(e.getProbId()));
		return probIdList;
	}

	// 문제 풀이 예상 시간 측정
	public Integer getSumEstimatedTime(List<Problem> probList) {
		Integer getSumEstimatedTime = 0;
		for (Problem prob : probList) {
			Float probDBTime = prob.getTimeRecommendation();
			Integer probTime = (probDBTime == null || probDBTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);
			getSumEstimatedTime += probTime;
		}
		return getSumEstimatedTime;
	}

	// 카드 안에 난이도 별로 문제 담기
	public void addAllProblemSetList(CardDTOV2 card, DiffProbListDTO diffProbList, Integer MIN_PROBLEM_NUM, Integer MAX_PROBLEM_NUM) {
		List<ProblemSetListDTO> problemSetList = card.getProbIdSetList();
		Integer estimatedTime = card.getEstimatedTime();

		// slice 최대 크기
		List<Problem> highProbList, middleProbList, lowProbList;
		highProbList = sliceProbList(diffProbList.getHighProbList(), MAX_PROBLEM_NUM);
		middleProbList = sliceProbList(diffProbList.getMiddleProbList(), MAX_PROBLEM_NUM);
		lowProbList = sliceProbList(diffProbList.getLowProbList(), MAX_PROBLEM_NUM);

		// Add
		ProblemSetListDTO probSetListDTO = ProblemSetListDTO.builder()
															.high(getIdListFromProbList(highProbList))
															.middle(getIdListFromProbList(middleProbList))
															.low(getIdListFromProbList(lowProbList))
															.min(MIN_PROBLEM_NUM)
															.max(MAX_PROBLEM_NUM)
															.build();
		problemSetList.add(probSetListDTO);

		// get estimated time
		Integer avgEstimatedTime = (getSumEstimatedTime(highProbList) + getSumEstimatedTime(middleProbList) + getSumEstimatedTime(lowProbList))
			/ (highProbList.size() + middleProbList.size() + lowProbList.size());
		estimatedTime += avgEstimatedTime * MAX_PROBLEM_NUM;

		card.setEstimatedTime(estimatedTime);
		card.setProbIdSetList(problemSetList);
	}

	// 카드 안에 난이도 확정 문제 담기
	public void addRatioProblemSetList(CardDTOV2 card, DiffProbListDTO diffProbList, Integer MIN_PROB_NUM, Integer MAX_PROBLEM_NUM, List<Integer> probDiffRatio) {
		List<ProblemSetListDTO> problemSetList = card.getProbIdSetList();
		Integer estimatedTime = card.getEstimatedTime();
		
		ProblemSetListDTO probSetListDTO = ProblemSetListDTO.builder()
															.min(MIN_PROB_NUM)
															.max(MAX_PROBLEM_NUM)
															.build();
		// 긴 순서 난이도 리턴. ex) ["상", "하", "중"]
		List<String> lengthOrderedDiffList = diffProbList.getLenOrderedDiffList();
		Integer probIdx = 0;
		for (String diff : lengthOrderedDiffList) {
			List<Problem> probList = diffProbList.getDiffProbList(diff);
			Integer diffIdx = 0;
			Integer DIFF_MAX_PROB = MID_EXAM_CARD_HIGH_PROB;
			switch (diff) {
				case "상":
					diffIdx = 0;
					DIFF_MAX_PROB = MID_EXAM_CARD_HIGH_PROB;
					break;
				case "중":
					diffIdx = 1;
					DIFF_MAX_PROB = MID_EXAM_CARD_MIDDLE_PROB;
					break;
				case "하":
					diffIdx = 2;
					DIFF_MAX_PROB = MID_EXAM_CARD_LOW_PROB;
					break;
			}
			Integer currentProbNum = probDiffRatio.get(diffIdx);
			if (currentProbNum < DIFF_MAX_PROB) {
				Integer addableProbNum = Math.min(DIFF_MAX_PROB - currentProbNum, probList.size());
				for (int i = 0; i < addableProbNum; i++) {
					if (printProbInfo)	logger.info("	probId={}({}) 추가", probList.get(i).getProbId(), diff);
					probSetListDTO.addDiffProb(probList.get(i).getProbId(), diff);
					Float probDBTime = probList.get(i).getTimeRecommendation();
					Integer probTime = (probDBTime == null || probDBTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);
					estimatedTime += probTime;
					probIdx += 1;
					probDiffRatio.set(diffIdx, probDiffRatio.get(diffIdx) + 1);
					if (probIdx == MAX_PROBLEM_NUM)
						break;
				}
			}
			if (probIdx == MAX_PROBLEM_NUM)
				break;
		}
		// 맞는 난이도가 없으면 난이도 랜덤하게 채우기
		if (probIdx != MAX_PROBLEM_NUM) {
			for (String diff : lengthOrderedDiffList) {
				List<Problem> probList = diffProbList.getDiffProbList(diff);
				for (Problem prob : probList) {
					if (!probSetListDTO.getDiffProbIdList(diff).contains(prob.getProbId())) {
						if (printProbInfo)	logger.info("	probId={}({}) 추가", prob.getProbId(), diff);
						probSetListDTO.addDiffProb(prob.getProbId(), diff);
						Float probDBTime = prob.getTimeRecommendation();
						Integer probTime = (probDBTime == null || probDBTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);
						estimatedTime += probTime;
						probIdx += 1;
						if (probIdx == MAX_PROBLEM_NUM)
							break;
					}
				}
				if (probIdx == MAX_PROBLEM_NUM)
					break;
			}
		}
		problemSetList.add(probSetListDTO);
		card.setProbIdSetList(problemSetList);
		card.setEstimatedTime(estimatedTime);
	}

	public void addCurrProblemWithFrequent(CardDTOV2 card, String superCurrId, String superCurrType, Integer probNum, Boolean isAdaptive,
		List<Integer> probDiffRatio, Integer verbose) {
		// superCurr 내의 모든 curr 불러오기
		List<String> freqOrderedCurrIdList = new ArrayList<String>();
		Map<String, Integer> currProbNumMap = new HashMap<String, Integer>();

		switch (superCurrType) {
			case "section":
				freqOrderedCurrIdList = problemTypeRepo.findSubSectionIdListInSectionOrderByFreq(superCurrId);
				break;
			case "chapter":
				freqOrderedCurrIdList = problemTypeRepo.findSectionIdListInChapterOrderByFreq(superCurrId);
				break;
			case "exam":
				freqOrderedCurrIdList = problemTypeRepo.findChapterListInSubSectionSetOrderByFreq(examSubSectionIdSet);
				break;
		}

		if (printCurrInfo && verbose != 0)
			logger.info("{} {} 내 단원 들 = ", superCurrType, superCurrId);

		// define prob num for each currId
		int probCnt = 0;
		while (probCnt != probNum) {
			for (String currId : freqOrderedCurrIdList) {
				if (problemRepo.findProbListByCurrId(currId, solvedProbIdSet).size() != 0) {
					if (currProbNumMap.containsKey(currId))
						currProbNumMap.put(currId, currProbNumMap.get(currId) + 1);
					else
						currProbNumMap.put(currId, 1);
					probCnt += 1;
				}
				if (probCnt == probNum)
					break;
			}
		}
		// 커리큘럼 순서대로 카드 내 단원 문제들 배치
		List<String> seqOrderedCurrIdList = curriculumRepo.sortByCurrSeq(currProbNumMap.keySet());
		CurrMasteryDTO currMastery;
		JsonObject cardDetailJson = new JsonObject();
		for (String currId : seqOrderedCurrIdList) {
			Integer currProbNum = currProbNumMap.get(currId);
			switch (superCurrType) {
				case "section":
					if (printCurrInfo && verbose != 0)
						logger.info("소단원 {} 내에서 {} 문제 출제", currId, currProbNum);
					currMastery = userKnowledgeRepo.findMasteryBySubSection(userId, currId);
					addSubSectionProblemWithFrequent(card, currId, currProbNum, isAdaptive, probDiffRatio, verbose);
					cardDetailJson.addProperty(currMastery.getCurrName(), currMastery.getMastery() * 100.0f);
					break;
				case "chapter":
					if (printCurrInfo && verbose != 0) {
						logger.info("");
						logger.info("중단원 {} 내에서 {} 문제 출제", currId, currProbNum);
					}
					currMastery = userKnowledgeRepo.findMasteryBySection(userId, currId);
					addCurrProblemWithFrequent(card, currId, "section", currProbNum, isAdaptive, probDiffRatio, verbose);
					cardDetailJson.addProperty(currMastery.getCurrName(), currMastery.getMastery() * 100.0f);
					break;
				case "exam":
					if (printCurrInfo && verbose != 0) {
						logger.info("");
						logger.info("대단원 {} 내에서 {} 문제 출제", currId, currProbNum);
					}
					currMastery = userKnowledgeRepo.findMasteryByChapter(userId, currId);
					addCurrProblemWithFrequent(card, currId, "chapter", currProbNum, isAdaptive, probDiffRatio, verbose);
					cardDetailJson.addProperty(currMastery.getCurrName(), currMastery.getMastery() * 100.0f);
					break;
			}
		}
		card.setCardDetail(cardDetailJson.toString());
	}

	// 시험/대/중단원 (superCurr) 내에 대/중/소단원 (curr) 별 난이도 고려하여 문제 추출 모듈
	public void addCurrProblemWithMastery(CardDTOV2 card, String superCurrId, String superCurrType, Integer probNum, Integer verbose) {
		// superCurr 내의 모든 curr 불러오기
		List<CurrMasteryDTO> currMasteryList = new ArrayList<CurrMasteryDTO>();
		Map<String, Integer> currProbNumMap = new HashMap<String, Integer>();

		switch (superCurrType) {
			case "section":
				currMasteryList = userKnowledgeRepo.findMasteryListInSectionOrderByMastery(userId, superCurrId);
				break;
			case "chapter":
				currMasteryList = userKnowledgeRepo.findMasteryListInChapterOrderByMastery(userId, superCurrId);
				break;
			case "exam":
				currMasteryList = userKnowledgeRepo.findMasteryListInSubSectionSetOrderByMastery(userId, examSubSectionIdSet);
				break;
		}

		if (printMastery && verbose != 0) {
			logger.info("{} {} 내 단원 들 = ", superCurrType, superCurrId);
			for (CurrMasteryDTO currMastery : currMasteryList)
				logger.info("{}\t{}\t(mastery={})", currMastery.getCurrId(), currMastery.getCurrName(), currMastery.getMastery());
		}
		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		while (probCnt != probNum) {
			for (CurrMasteryDTO currMastery : currMasteryList) {
				String currId = currMastery.getCurrId();
				String currName = currMastery.getCurrName();
				Float mastery = currMastery.getMastery();
				if (problemRepo.findProbListByCurrId(currId, solvedProbIdSet).size() != 0) {
					if (currProbNumMap.containsKey(currId))
						currProbNumMap.put(currId, currProbNumMap.get(currId) + 1);
					else {
						currProbNumMap.put(currId, 1);
						cardDetailJson.addProperty(currName, mastery * 100.0f);
					}
					probCnt += 1;
				}
				if (probCnt == probNum)
					break;
			}
		}
		// 커리큘럼 순서대로 카드 내 단원 문제들 배치
		List<String> seqOrderedCurrIdList = curriculumRepo.sortByCurrSeq(currProbNumMap.keySet());
		for (String currId : seqOrderedCurrIdList) {
			Integer currProbNum = currProbNumMap.get(currId);
			switch (superCurrType) {
				case "section":
					if (printCurrInfo && verbose != 0)
						logger.info("소단원 {} 내에서 {} 문제 출제", currId, currProbNum);
					addSubSectionProblemWithMastery(card, currId, currProbNum, verbose);
					break;
				case "chapter":
					if (printCurrInfo && verbose != 0) {
						logger.info("");
						logger.info("중단원 {} 내에서 {} 문제 출제", currId, currProbNum);
					}
					addCurrProblemWithMastery(card, currId, "section", currProbNum, verbose);
					break;
				case "exam":
					if (printCurrInfo && verbose != 0) {
						logger.info("");
						logger.info("대단원 {} 내에서 {} 문제 출제", currId, currProbNum);
					}
					addCurrProblemWithMastery(card, currId, "chapter", currProbNum, verbose);
					break;
			}
		}
		card.setCardDetail(cardDetailJson.toString());
	}

	// 소단원 내 문제 출제 모듈. 빈출 유형인 것들 중에 출제
	public void addSubSectionProblemWithFrequent(CardDTOV2 card, String subSectionId, Integer probNum, Boolean isAdaptive,
		List<Integer> probDiffRatio, Integer verbose) {
		List<Integer> freqTypeIdList = problemTypeRepo.findFreqTypeIdListInSubSection(subSectionId);
		Map<Integer, Integer> typeProbNumMap = new HashMap<Integer, Integer>();
		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		while (probCnt != probNum) {
			for (Integer typeId : freqTypeIdList) {
				if (problemRepo.NfindProbListByType(typeId, solvedProbIdSet).size() != 0) {
					if (typeProbNumMap.containsKey(typeId))
						typeProbNumMap.put(typeId, typeProbNumMap.get(typeId) + 1);
					else
						typeProbNumMap.put(typeId, 1);
					probCnt += 1;
				}
				if (probCnt == probNum)
					break;
			}
		}
		// 커리큘럼 순서대로 카드 내 유형 문제들 구성
		List<Integer> seqOrderedTypeidList = problemTypeRepo.sortByTypeSeq(typeProbNumMap.keySet());
		for (Integer typeId : seqOrderedTypeidList) {
			Integer typeProbNum = typeProbNumMap.get(typeId);
			if (printTypeInfo && verbose != 0)
				logger.info("	유형 {} 내에서 {} 문제 출제", typeId, typeProbNum);
			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			if (isAdaptive)
				addAllProblemSetList(card, diffProbList, typeProbNum, typeProbNum);
			else
				addRatioProblemSetList(card, diffProbList, typeProbNum, typeProbNum, probDiffRatio);
		}
		card.setCardDetail(cardDetailJson.toString());
	}

	// 소단원 내 문제 출제 모듈. 마스터리가 낮은 유형 순서대로 많이 출제
	public void addSubSectionProblemWithMastery(CardDTOV2 card, String subSectionId, Integer probNum, Integer verbose) {
		List<Integer> typeIdList = problemTypeRepo.findTypeIdListInSubSection(subSectionId);
		List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findTypeMasteryList(userId, typeIdList);
		Map<Integer, Integer> typeProbNumMap = new HashMap<Integer, Integer>();
		if (printMastery && verbose != 0) {
			logger.info("소단원 {} 내 유형들 = ", subSectionId);
			for (TypeMasteryDTO typeMastery : typeMasteryList)
				logger.info("{} (mastery={})", typeMastery.getTypeId(), typeMastery.getMastery());
		}
		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		while (probCnt != probNum) {
			for (TypeMasteryDTO typeMastery : typeMasteryList) {
				Integer typeId = typeMastery.getTypeId();
				if (problemRepo.NfindProbListByType(typeId, solvedProbIdSet).size() != 0) {
					if (typeProbNumMap.containsKey(typeId))
						typeProbNumMap.put(typeId, typeProbNumMap.get(typeId) + 1);
					else
						typeProbNumMap.put(typeId, 1);
					probCnt += 1;
				}
				if (probCnt == probNum)
					break;
			}
		}
		List<Integer> seqOrderedTypeidList = problemTypeRepo.sortByTypeSeq(typeProbNumMap.keySet());
		for (Integer typeId : seqOrderedTypeidList) {
			Integer typeProbNum = typeProbNumMap.get(typeId);
			if (printTypeInfo && verbose != 0)
				logger.info("	유형 {} 내에서 {} 문제 출제", typeId, typeProbNum);
			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			if (card.getProbIdSetList().size() == 0) {
				Float firstProbMastery = userKnowledgeRepo.findTypeMastery(userId, typeId).getMastery();
				card.setFirstProbLevel(getFirstProbLevel(firstProbMastery));
			}
			addAllProblemSetList(card, diffProbList, typeProbNum, typeProbNum);
		}
		card.setCardDetail(cardDetailJson.toString());
	}

	// 실력 향상 - 유형카드
	public CardDTOV2 generateTypeCard(Integer typeId) {
		String cardTitle = problemTypeRepo.findTypeNameById(typeId);
		// 유형 카드 상세 정보
		JsonObject cardDetailJson = new JsonObject();
		Curriculum typeCurriculum = curriculumRepo.findByType(typeId);
		cardDetailJson.addProperty("subSection", typeCurriculum.getSubSection());
		cardDetailJson.addProperty("section", typeCurriculum.getSection());
		cardDetailJson.addProperty("chapter", typeCurriculum.getChapter());
		// 유형 점수
		TypeMasteryDTO typeMastery = userKnowledgeRepo.findTypeMastery(userId, typeId);
		Float mastery = typeMastery != null ? typeMastery.getMastery() : 0.6328f;
		CardDTOV2 typeCard = CardDTOV2.builder()
									  .cardType(TYPE_CARD_TYPE)
									  .cardTitle(cardTitle)
									  .probIdSetList(new ArrayList<ProblemSetListDTO>())
									  .estimatedTime(0)
									  .cardDetail(cardDetailJson.toString())
									  .cardScore(mastery* 100)
									  .build();

		List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
		if (typeProbList.size() == 0)
			return CardDTOV2.builder().build();
		else {
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			addAllProblemSetList(typeCard, diffProbList, MIN_TYPE_CARD_PROB_NUM, MAX_TYPE_CARD_PROB_NUM);
			typeCard.setFirstProbLevel(getFirstProbLevel(mastery));
			return typeCard;
		}
	}

	// 실력 향상 - 중간 평가 카드 (type="section")
	public CardDTOV2 generateNormalMidExamCard(String curriculumId, String type) {
		CurrMasteryDTO mastery;
		if (type.equals("section"))
			mastery = userKnowledgeRepo.findSectionMastery(userId, curriculumId);
		else
			mastery = userKnowledgeRepo.findChapterMastery(userId, curriculumId);
		logger.info("{}, {}, {}", mastery.getCurrId(), mastery.getCurrName(), mastery.getMastery());
		List<Integer> probDiffRatio = new ArrayList<Integer>(Arrays.asList(0, 0, 0));
		CardDTOV2 midExamCard = CardDTOV2.builder()
										 .cardType(SECTION_MID_EXAM_CARD_TYPE)
										 .cardTitle(mastery.getCurrName())
										 .probIdSetList(new ArrayList<ProblemSetListDTO>())
										 .estimatedTime(0)
										 .cardScore(mastery.getMastery() * 100)
										 .build();
		addCurrProblemWithFrequent(midExamCard, curriculumId, type, MID_EXAM_CARD_PROB_NUM, false, probDiffRatio, 1);
		return midExamCard;
	}

	// 실력 향상 - 보충카드
	public CardDTOV2 generateSupplementCard(List<TypeMasteryDTO> lowMasteryTypeList) {
		String cardTitle = String.format(SUPPLEMENT_CARD_TITLE_FORMAT, lowMasteryTypeList.size());
		CardDTOV2 supplementCard = CardDTOV2.builder()
											.cardType(SUPPLEMENT_CARD_TYPE)
											.cardTitle(cardTitle)
											.probIdSetList(new ArrayList<ProblemSetListDTO>())
											.estimatedTime(0)
											.build();
		
		JsonObject cardDetailJson = new JsonObject();
		int cnt = 1;
		for (TypeMasteryDTO typeMastery : lowMasteryTypeList) {
			Integer typeId = typeMastery.getTypeId();
			String typeName = problemTypeRepo.NfindTypeNameById(typeId);
			Float mastery = typeMastery.getMastery();
			logger.info("보충카드 {}번째 유형 = {} (mastery={}) {} 문제", cnt, typeId, mastery, SUPPLE_CARD_PROB_NUM_PER_TYPE);

			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, null);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			addAllProblemSetList(supplementCard, diffProbList, SUPPLE_CARD_PROB_NUM_PER_TYPE, SUPPLE_CARD_PROB_NUM_PER_TYPE);

			cardDetailJson.addProperty(typeName, mastery * 100.0f);
			if (cnt == 1)
				supplementCard.setFirstProbLevel(getFirstProbLevel(mastery));
			cnt += 1;
		}
		supplementCard.setCardDetail(cardDetailJson.toString());
		return supplementCard;
	}

	// 시험 대비 - 모의고사 카드
	public CardDTOV2 generateTrialExamCard(String trialExamType) {
		String cardTitle = "";
		String[] trialExamInfo = trialExamType.split("-");
		if (trialExamInfo[2].equals("mid"))
			cardTitle = String.format(TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0], trialExamInfo[1], "중간고사");
		else if (trialExamInfo[2].equals("final"))
			cardTitle = String.format(TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0], trialExamInfo[1], "기말고사");
		CurrMasteryDTO mastery = userKnowledgeRepo.findExamMastery(userId, examSubSectionIdSet);
		CardDTOV2 trialExamCard = CardDTOV2.builder()
										   .cardType(TRIAL_EXAM_CARD_TYPE)
										   .cardTitle(cardTitle)
										   .probIdSetList(new ArrayList<ProblemSetListDTO>())
										   .estimatedTime(0)
										   .cardScore(mastery.getMastery() * 100)
										   .firstProbLevel("middle")
										   .build();
		addCurrProblemWithMastery(trialExamCard, "모의고사", "exam", MID_EXAM_CARD_PROB_NUM, 1);
		// 첫 문제 레벨
		trialExamCard.setFirstProbLevel("middle");
		return trialExamCard;
	}

	public CardDTOV2 generateCard(CardConfigDTO cardConfig) {
		CardDTOV2 card = null;
		switch (cardConfig.getCardType()) {
			case "type":
				logger.info("------ {} card (type {})", cardConfig.getCardType(), cardConfig.getTypeId());
				card = generateTypeCard(cardConfig.getTypeId());
				break;
			case "supple":
				logger.info("------ {} card", cardConfig.getCardType());
				card = generateSupplementCard(cardConfig.getLowMasteryTypeList());
				break;
			case "midExam":
				logger.info("------ {} card ({} {})", cardConfig.getCardType(), cardConfig.getMidExamType(), cardConfig.getMidExamCurriculumId());
				card = generateNormalMidExamCard(cardConfig.getMidExamCurriculumId(), cardConfig.getMidExamType());
				break;
			case "trialExam":
				logger.info("------ {} card ({})", cardConfig.getCardType(), cardConfig.getTrialExamType());
				card = generateTrialExamCard(cardConfig.getTrialExamType());
				break;
		}
		return card;
	}
}
