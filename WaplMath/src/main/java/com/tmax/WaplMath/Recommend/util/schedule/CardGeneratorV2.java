package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	
	public String getFirstProbLevel(Float mastery, List<String> existDiffList) {
		if (mastery >= CardConstants.MASTERY_HIGH_THRESHOLD) {
			if (existDiffList.contains("상"))
				return "high";
			else if (existDiffList.contains("중"))
				return "middle";
			else
				return "low";
		} else if (mastery < CardConstants.MASTERY_HIGH_THRESHOLD && mastery >= CardConstants.MASTERY_LOW_THRESHOLD) {
			if (existDiffList.contains("중"))
				return "middle";
			else {
				Float differenceToLow = mastery - CardConstants.MASTERY_LOW_THRESHOLD;
				Float differenceToHigh = CardConstants.MASTERY_HIGH_THRESHOLD - mastery;
				if (differenceToLow < differenceToHigh) {
					if (existDiffList.contains("하"))
						return "low";
					else
						return "high";
				} else {
					if (existDiffList.contains("상"))
						return "high";
					else
						return "low";
				}
			}
		} else {
			if (existDiffList.contains("하"))
				return "low";
			else if (existDiffList.contains("중"))
				return "middle";
			else
				return "high";
		}
	}

	// 문제 난이도 별 문제 Id 모아놓는 모듈.
	public DiffProbListDTO generateDiffProbList(List<Problem> probList) {
		DiffProbListDTO diffProbList = new DiffProbListDTO();
		for (Problem prob : probList) {	diffProbList.addDiffProb(prob, prob.getDifficulty()); }
		return diffProbList;
	}

	public void printDiffProbList(DiffProbListDTO diffProbList) {
		if (printProbInfo) {
			for (String difficulty : Arrays.asList("상", "중", "하")) {
				List<Problem> probList = diffProbList.getDiffProbList(difficulty);
				logger.info(String.format("	{} 난이도 문제들 = {}", difficulty, getIdListFromProbList(probList)));
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
		return probList.stream().map(prob -> prob.getProbId()).collect(Collectors.toList());
	}

	// 문제 풀이 예상 시간 측정
	public Integer getSumEstimatedTime(List<Problem> probList) {
		Integer getSumEstimatedTime = 0;
		for (Problem prob : probList) {
			Float probDBTime = prob.getTimeRecommendation();
			Integer probTime = (probDBTime == null || probDBTime == 0.0f) ? CardConstants.AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);
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
		// add to card object
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
			Integer DIFF_MAX_PROB = CardConstants.SECTION_TEST_CARD_HIGH_PROB;
			switch (diff) {
				case "상":
					diffIdx = 0;
					DIFF_MAX_PROB = CardConstants.SECTION_TEST_CARD_HIGH_PROB;
					break;
				case "중":
					diffIdx = 1;
					DIFF_MAX_PROB = CardConstants.SECTION_TEST_MIDDLE_PROB;
					break;
				case "하":
					diffIdx = 2;
					DIFF_MAX_PROB = CardConstants.SECTION_TEST_LOW_PROB;
					break;
			}
			Integer currentProbNum = probDiffRatio.get(diffIdx);
			if (currentProbNum < DIFF_MAX_PROB) {
				Integer addableProbNum = Math.min(DIFF_MAX_PROB - currentProbNum, probList.size());
				for (int i = 0; i < addableProbNum; i++) {
					if (printProbInfo)	logger.info("	probId={}({}) 추가", probList.get(i).getProbId(), diff);
					probSetListDTO.addDiffProb(probList.get(i).getProbId(), diff);
					Float probDBTime = probList.get(i).getTimeRecommendation();
					Integer probTime = (probDBTime == null || probDBTime == 0.0f) ? CardConstants.AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);
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
						Integer probTime = (probDBTime == null || probDBTime == 0.0f) ? CardConstants.AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);
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
		List<String> freqOrderedCurrIdList = new ArrayList<String>();
		Map<String, Integer> currentCurrProbNumMap = new HashMap<String, Integer>();
		Map<String, Integer> totalCurrProbNumMap = new HashMap<String, Integer>();
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
		Set<String> noProbCurrIdSet = new HashSet<String>();
		while (probCnt != probNum) {
			for (String currId : freqOrderedCurrIdList) {
				Integer currProbNum = currentCurrProbNumMap.containsKey(currId) ? currentCurrProbNumMap.get(currId) : 0;
				Integer totalCurrProbNum;
				if (!totalCurrProbNumMap.containsKey(currId)) {
					totalCurrProbNum = problemRepo.findProbListByCurrId(currId, solvedProbIdSet).size();
					totalCurrProbNumMap.put(currId, totalCurrProbNum);
				} else
					totalCurrProbNum = totalCurrProbNumMap.get(currId);
				if (currProbNum < totalCurrProbNum) {
					currentCurrProbNumMap.put(currId, currProbNum + 1);
					probCnt += 1;
				} else
					noProbCurrIdSet.add(currId);
				if (probCnt == probNum)
					break;
			}
			if (noProbCurrIdSet.size() == freqOrderedCurrIdList.size())
				break;
		}
		// 커리큘럼 순서대로 카드 내 단원 문제들 배치
		List<String> seqOrderedCurrIdList = curriculumRepo.sortByCurrSeq(currentCurrProbNumMap.keySet());
		CurrMasteryDTO currMastery;
		JsonObject cardDetailJson = new JsonObject();
		for (String currId : seqOrderedCurrIdList) {
			Integer currProbNum = currentCurrProbNumMap.get(currId);
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
		Map<String, Integer> currentCurrProbNumMap = new HashMap<String, Integer>();
		Map<String, Integer> totalCurrProbNumMap = new HashMap<String, Integer>();
		Map<String, Integer> currTypeNumMap = new HashMap<String, Integer>();
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
		Set<String> noProbCurrIdSet = new HashSet<String>();
		Set<String> fullCurrIdSet = new HashSet<String>();
		while (probCnt != probNum) {
			Integer probPerType = 1; // 유형마다 포함되는 최대 문제수 --> 최대한 많은 범위 커버를 위함
			for (CurrMasteryDTO currMastery : currMasteryList) {
				String currId = currMastery.getCurrId();
				String currName = currMastery.getCurrName();
				Float mastery = currMastery.getMastery();
				// currId 에 현재 할당된 문제 수
				Integer currProbNum = currentCurrProbNumMap.containsKey(currId) ? currentCurrProbNumMap.get(currId) : 0;
				Integer typeNum;
				if (superCurrType.equals("section")) {
					if (!currTypeNumMap.containsKey(currId)) {
						typeNum = problemTypeRepo.findTypeIdListInSubSection(currId).size();
						currTypeNumMap.put(currId, typeNum);
					} else {
						typeNum = currTypeNumMap.get(currId);
					}
				} else
					typeNum = currProbNum;
				if (currProbNum <= typeNum * probPerType) {
					// currId 내 모든 문제 수
					Integer totalCurrProbNum;
					if (!totalCurrProbNumMap.containsKey(currId)) {
						totalCurrProbNum = problemRepo.findProbListByCurrId(currId, solvedProbIdSet).size();
						totalCurrProbNumMap.put(currId, totalCurrProbNum);
					} else
						totalCurrProbNum = totalCurrProbNumMap.get(currId);
					// 한 문제 더 들어갈 문제가 있다
					if (totalCurrProbNum > currProbNum) {
						currentCurrProbNumMap.put(currId, currProbNum + 1);
						cardDetailJson.addProperty(currName, mastery * 100.0f);
						probCnt += 1;
					} else // 문제가 없는 id
						noProbCurrIdSet.add(currId);
				} else // 유형마다 포함되는 최대 문제수만큼 꽉차있는 소단원들
					fullCurrIdSet.add(currId);
				if (probCnt == probNum)
					break;
			}
			if (currMasteryList.size() == noProbCurrIdSet.size())
				break;
			if (currMasteryList.size() == fullCurrIdSet.size())
				probPerType += 1;// 유형마다 포함되는 최대 문제수 + 1
		}
		// 커리큘럼 순서대로 카드 내 단원 문제들 배치
		List<String> seqOrderedCurrIdList = curriculumRepo.sortByCurrSeq(currentCurrProbNumMap.keySet());
		for (String currId : seqOrderedCurrIdList) {
			Integer currProbNum = currentCurrProbNumMap.get(currId);
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
		Map<Integer, Integer> currentTypeProbNumMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> totalTypeProbNumMap = new HashMap<Integer, Integer>();
		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		Set<Integer> noProbTypeIdSet = new HashSet<Integer>();
		while (probCnt != probNum) {
			for (Integer typeId : freqTypeIdList) {
				Integer typeProbNum = currentTypeProbNumMap.containsKey(typeId) ? currentTypeProbNumMap.get(typeId) : 0;
				// typeId 내 모든 문제 수
				Integer totalTypeProbNum;
				if (!totalTypeProbNumMap.containsKey(typeId)) {
					totalTypeProbNum = problemRepo.NfindProbListByType(typeId, solvedProbIdSet).size();
					totalTypeProbNumMap.put(typeId, totalTypeProbNum);
				} else
					totalTypeProbNum = totalTypeProbNumMap.get(typeId);
				// 한 문제 더 들어갈 문제가 있다
				if (totalTypeProbNum > typeProbNum) {
					currentTypeProbNumMap.put(typeId, typeProbNum + 1);
					probCnt += 1;
				} else
					noProbTypeIdSet.add(typeId);
				if (probCnt == probNum)
					break;
			}
			if (freqTypeIdList.size() == noProbTypeIdSet.size())
				break;
		}
		// 커리큘럼 순서대로 카드 내 유형 문제들 구성
		List<Integer> seqOrderedTypeidList = problemTypeRepo.sortByTypeSeq(currentTypeProbNumMap.keySet());
		for (Integer typeId : seqOrderedTypeidList) {
			Integer typeProbNum = currentTypeProbNumMap.get(typeId);
			if (printTypeInfo && verbose != 0)
				logger.info("	유형 {} 내에서 {} 문제 출제", typeId, typeProbNum);
			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			if (card.getProbIdSetList().size() == 0) {
				Float firstProbMastery = userKnowledgeRepo.findTypeMastery(userId, typeId).getMastery();
				card.setFirstProbLevel(getFirstProbLevel(firstProbMastery, diffProbList.getExistDiffList()));
			}
			if (isAdaptive)
				addAllProblemSetList(card, diffProbList, typeProbNum, typeProbNum);
			else
				addRatioProblemSetList(card, diffProbList, typeProbNum, typeProbNum, probDiffRatio);
		}
		card.setCardDetail(cardDetailJson.toString());
	}

	// 소단원 내 문제 출제 모듈. 안본 유형 + 마스터리가 낮은 유형 순서대로 많이 출제
	public void addSubSectionProblemWithMastery(CardDTOV2 card, String subSectionId, Integer probNum, Integer verbose) {
		List<Integer> typeIdList = problemTypeRepo.findTypeIdListInSubSection(subSectionId);
		List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findTypeMasteryList(userId, typeIdList);		
		Map<Integer, Integer> currentTypeProbNumMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> totalTypeProbNumMap = new HashMap<Integer, Integer>();
		if (printMastery && verbose != 0) {
			logger.info("소단원 {} 내 유형들 = ", subSectionId);
			for (TypeMasteryDTO typeMastery : typeMasteryList)
				logger.info("{} (mastery={})", typeMastery.getTypeId(), typeMastery.getMastery());
		}
		JsonObject cardDetailJson = new JsonObject();
		int probCnt = 0;
		Set<Integer> noProbTypeIdSet = new HashSet<Integer>();
		while (probCnt != probNum) {
			for (TypeMasteryDTO typeMastery : typeMasteryList) {
				Integer typeId = typeMastery.getTypeId();
				Integer typeProbNum = currentTypeProbNumMap.containsKey(typeId) ? currentTypeProbNumMap.get(typeId) : 0;
				// typeId 내 모든 문제 수
				Integer totalTypeProbNum;
				if (!totalTypeProbNumMap.containsKey(typeId)) {
					totalTypeProbNum = problemRepo.NfindProbListByType(typeId, solvedProbIdSet).size();
					totalTypeProbNumMap.put(typeId, totalTypeProbNum);
				} else
					totalTypeProbNum = totalTypeProbNumMap.get(typeId);
				// 한 문제 더 들어갈 문제가 있다
				if (totalTypeProbNum > typeProbNum) {
					currentTypeProbNumMap.put(typeId, typeProbNum + 1);
					probCnt += 1;
				} else
					noProbTypeIdSet.add(typeId);
				if (probCnt == probNum)
					break;
			}
			if (typeMasteryList.size() == noProbTypeIdSet.size())
				break;
		}
		List<Integer> seqOrderedTypeidList = problemTypeRepo.sortByTypeSeq(currentTypeProbNumMap.keySet());
		for (Integer typeId : seqOrderedTypeidList) {
			Integer typeProbNum = currentTypeProbNumMap.get(typeId);
			if (printTypeInfo && verbose != 0)
				logger.info("	유형 {} 내에서 {} 문제 출제", typeId, typeProbNum);
			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			if (card.getProbIdSetList().size() == 0) {
				Float firstProbMastery = userKnowledgeRepo.findTypeMastery(userId, typeId).getMastery();
				card.setFirstProbLevel(getFirstProbLevel(firstProbMastery, diffProbList.getExistDiffList()));
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
									  .cardType(CardConstants.TYPE_CARD_TYPESTR)
									  .cardTitle(cardTitle)
									  .probIdSetList(new ArrayList<ProblemSetListDTO>())
									  .estimatedTime(0)
									  .cardDetail(cardDetailJson.toString())
									  .cardScore(mastery* 100)
									  .build();
		// 유형 내 문제들 리턴
		List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
		if (typeProbList.size() == 0)
			return CardDTOV2.builder().build();
		else {
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			addAllProblemSetList(typeCard, diffProbList, CardConstants.MIN_TYPE_CARD_PROB_NUM, CardConstants.MAX_TYPE_CARD_PROB_NUM);
			typeCard.setFirstProbLevel(getFirstProbLevel(mastery, diffProbList.getExistDiffList()));
			return typeCard;
		}
	}

	// 실력 향상 - 중간 평가 카드 (type="section"/"chapter")
	public CardDTOV2 generateTestCard(String curriculumId, String type, String cardType) {
		CurrMasteryDTO mastery;
		if (type.equals("section"))
			mastery = userKnowledgeRepo.findSectionMastery(userId, curriculumId);
		else
			mastery = userKnowledgeRepo.findChapterMastery(userId, curriculumId);
		logger.info("{}, {}, {}", mastery.getCurrId(), mastery.getCurrName(), mastery.getMastery());
		List<Integer> probDiffRatio = new ArrayList<Integer>(Arrays.asList(0, 0, 0));
		CardDTOV2 testCard = CardDTOV2.builder()
										 .cardType(cardType)
										 .cardTitle(mastery.getCurrName())
										 .probIdSetList(new ArrayList<ProblemSetListDTO>())
										 .estimatedTime(0)
										 .cardScore(mastery.getMastery() * 100)
										 .build();
		addCurrProblemWithFrequent(testCard, curriculumId, type, CardConstants.MAX_CARD_PROB_NUM, false, probDiffRatio, 1);
		return testCard;
	}

	// 실력 향상 - 보충카드
	public CardDTOV2 generateSupplementCard(List<TypeMasteryDTO> typeMasteryList) {
		String cardTitle = String.format(CardConstants.SUPPLE_CARD_TITLE_FORMAT, typeMasteryList.size());
		CardDTOV2 supplementCard = CardDTOV2.builder()
											.cardType(CardConstants.SUPPLE_CARD_TYPESTR)
											.cardTitle(cardTitle)
											.probIdSetList(new ArrayList<ProblemSetListDTO>())
											.estimatedTime(0)
											.build();
		
		JsonObject cardDetailJson = new JsonObject();
		int cnt = 1;
		for (TypeMasteryDTO typeMastery : typeMasteryList) {
			Integer typeId = typeMastery.getTypeId();
			String typeName = problemTypeRepo.NfindTypeNameById(typeId);
			Float mastery = typeMastery.getMastery();
			logger.info("보충카드 {}번째 유형 = {} (mastery={}) {} 문제", cnt, typeId, mastery, CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE);

			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, null);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			addAllProblemSetList(supplementCard, diffProbList, CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE, CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE);

			cardDetailJson.addProperty(typeName, mastery * 100.0f);
			if (cnt == 1)
				supplementCard.setFirstProbLevel(getFirstProbLevel(mastery, diffProbList.getExistDiffList()));
			cnt += 1;
		}
		supplementCard.setCardDetail(cardDetailJson.toString());
		return supplementCard;
	}

	// 실력 향상 - 추가 보충카드
	public CardDTOV2 generateAddtlSupplementCard(List<TypeMasteryDTO> typeMasteryList) {
		String cardTitle = String.format(CardConstants.ADDTL_SUPPLE_CARD_TITLE_FORMAT, typeMasteryList.size());
		CardDTOV2 supplementCard = CardDTOV2.builder()
											.cardType(CardConstants.ADDTL_SUPPLE_CARD_TYPESTR)
											.cardTitle(cardTitle)
											.probIdSetList(new ArrayList<ProblemSetListDTO>())
											.estimatedTime(0)
											.build();
		
		JsonObject cardDetailJson = new JsonObject();
		int cnt = 1;
		for (TypeMasteryDTO typeMastery : typeMasteryList) {
			Integer typeId = typeMastery.getTypeId();
			String typeName = problemTypeRepo.NfindTypeNameById(typeId);
			Float mastery = typeMastery.getMastery();
			logger.info("추가 보충카드 {}번째 유형 = {} (mastery={}) {} 문제", cnt, typeId, mastery, CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE);

			List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, null);
			DiffProbListDTO diffProbList = generateDiffProbList(typeProbList);
			printDiffProbList(diffProbList);
			addAllProblemSetList(supplementCard, diffProbList, CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE, CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE);

			cardDetailJson.addProperty(typeName, mastery * 100.0f);
			if (cnt == 1)
				supplementCard.setFirstProbLevel(getFirstProbLevel(mastery, diffProbList.getExistDiffList()));
			cnt += 1;
		}
		supplementCard.setCardDetail(cardDetailJson.toString());
		return supplementCard;
	}

	// 시험 대비 - section_exam (한 중단원 범위 내 20개), full_scope_exam (중단원 내 4-5개) 카드
	public CardDTOV2 generateExamCard(String curriculumId, String cardType, Integer probNum) {
		CurrMasteryDTO mastery;
		mastery = userKnowledgeRepo.findSectionMastery(userId, curriculumId);
		// 카드 상세 정보
		JsonObject cardDetailJson = new JsonObject();
		Curriculum sectionCurriculum = curriculumRepo.findById(curriculumId).orElse(new Curriculum());
		cardDetailJson.addProperty("chapter", sectionCurriculum.getChapter());
		logger.info("{}, {}, {}", mastery.getCurrId(), mastery.getCurrName(), mastery.getMastery());
		CardDTOV2 examCard = CardDTOV2.builder()
									  .cardType(cardType)
									  .cardTitle(mastery.getCurrName())
									  .probIdSetList(new ArrayList<ProblemSetListDTO>())
									  .estimatedTime(0)
									  .cardScore(mastery.getMastery() * 100)
									  .cardDetail(cardDetailJson.toString())
									  .build();
		addCurrProblemWithMastery(examCard, curriculumId, "section", probNum, 1);
		return examCard;
	}
	
	// 시험 대비 - 모의고사 카드
	public CardDTOV2 generateTrialExamCard(String examKeyword) {
		String cardTitle = "";
		String[] trialExamInfo = examKeyword.split("-");
		if (trialExamInfo[2].equals("mid"))
			cardTitle = String.format(CardConstants.TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0], trialExamInfo[1], "중간고사");
		else if (trialExamInfo[2].equals("final"))
			cardTitle = String.format(CardConstants.TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0], trialExamInfo[1], "기말고사");
		CurrMasteryDTO mastery = userKnowledgeRepo.findExamMastery(userId, examSubSectionIdSet);
		CardDTOV2 trialExamCard = CardDTOV2.builder()
										   .cardType(CardConstants.TRIAL_EXAM_CARD_TYPESTR)
										   .cardTitle(cardTitle)
										   .probIdSetList(new ArrayList<ProblemSetListDTO>())
										   .estimatedTime(0)
										   .cardScore(mastery.getMastery() * 100)
										   .firstProbLevel("middle")
										   .build();
		addCurrProblemWithMastery(trialExamCard, "모의고사", "exam", CardConstants.MAX_CARD_PROB_NUM, 1);
		return trialExamCard;
	}

	public CardDTOV2 generateCard(CardConfigDTO cardConfig) {
		CardDTOV2 card = null;
		switch (cardConfig.getCardType()) {
			case CardConstants.TYPE_CARD_TYPESTR:
				logger.info("------ {} card (type {})", cardConfig.getCardType(), cardConfig.getTypeId());
				card = generateTypeCard(cardConfig.getTypeId());
				break;
			case CardConstants.SUPPLE_CARD_TYPESTR:
				logger.info("------ {} card", cardConfig.getCardType());
				card = generateSupplementCard(cardConfig.getTypeMasteryList());
				break;
			case CardConstants.SECTION_TEST_CARD_TYPESTR:
				logger.info("------ {} card ({})", cardConfig.getCardType(), cardConfig.getCurriculumId());
				card = generateTestCard(cardConfig.getCurriculumId(), "section", cardConfig.getCardType());
				break;
			case CardConstants.ADDTL_SUPPLE_CARD_TYPESTR:
				logger.info("------ {} card", cardConfig.getCardType());
				card = generateAddtlSupplementCard(cardConfig.getTypeMasteryList());
				break;
			case CardConstants.SECTION_EXAM_CARD_TYPESTR:
				logger.info("------ {} card ({} {} 문제)", cardConfig.getCardType(), cardConfig.getCurriculumId(), cardConfig.getProbNum());
				card = generateExamCard(cardConfig.getCurriculumId(), cardConfig.getCardType(), cardConfig.getProbNum());
				break;
			case CardConstants.FULL_SCOPE_EXAM_CARD_TYPESTR:
				logger.info("------ {} card ({} {} 문제)", cardConfig.getCardType(), cardConfig.getCurriculumId(), cardConfig.getProbNum());
				card = generateExamCard(cardConfig.getCurriculumId(), cardConfig.getCardType(), cardConfig.getProbNum());
				break;
			case CardConstants.TRIAL_EXAM_CARD_TYPESTR:
				logger.info("------ {} card ({})", cardConfig.getCardType(), cardConfig.getExamKeyword());
				card = generateTrialExamCard(cardConfig.getExamKeyword());
				break;
		}
		return card;
	}
}
