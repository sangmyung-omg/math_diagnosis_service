package com.tmax.WaplMath.Recommend.util.card;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Recommend.dto.mastery.CurrMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.DiffProbListDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ProblemSetListDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepo;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.config.CardConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Generate normal/exam schedule card information V2
 * @author Sangheon_lee
 * @since 2021-06-30
 */
@Slf4j
@Component
public class CardGenerator extends CardConstants {

  // logging option
  private boolean PRINT_PROB_INFO = false; // level 1
  private boolean PRINT_MASTERY = false; // level 2
  private boolean PRINT_TYPE_ID = true; // level 3
  private boolean PRINT_CURR_INFO = true; // level 4


  @Autowired
  @Qualifier("RE-ProblemRepo")
  private ProblemRepo problemRepo;

  @Autowired
  @Qualifier("RE-ProblemTypeRepo")
  private ProblemTypeRepo problemTypeRepo;

  @Autowired
  @Qualifier("RE-CurriculumRepo")
  private CurriculumRepo curriculumRepo;

  @Autowired
  @Qualifier("RE-UserKnowledgeRepo")
  private UserKnowledgeRepo userKnowledgeRepo;

  // user info vars
  private String userId;
  private Set<Integer> solvedProbIdSet; // 이미 푼 문제 Id set
  private Set<String> examSubSectionIdSet; // 시험 범위 내 소단원 Id set
  private String todayUTC;


  public enum CurrType {
    section, chapter, exam;

    private @Getter @Setter String currId;

    private CurrType () {}
  }


  // initialize generator variables
  public void initGenerator(String userId, Set<Integer> solvedProbIdSet, Set<String> examSubSectionIdSet){
    // set today utc date
    // 2021-09-01 Modified by Sangheon Lee. Get probs modified before today
    this.todayUTC = ZonedDateTime.now(ZoneId.of("UTC"))
                                 .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    log.info("Sampling probs before " + this.todayUTC);
    
    this.userId = userId;
    this.solvedProbIdSet = solvedProbIdSet;
    this.examSubSectionIdSet = examSubSectionIdSet;    
  }


  // set log level for debug mode
  public void setLogLevel(boolean debugMode){
    if (debugMode) {
      this.PRINT_PROB_INFO = true; 
      this.PRINT_MASTERY = true;
    }
  }


  // orderedDiffList 순서대로, 문제가 있으면 그 난이도를 리턴
  public String getExistLevel(List<String> existDiffStrList, List<Difficulty> orderedDiffList){
    for (Difficulty diff: orderedDiffList){			
      if (existDiffStrList.contains(diff.name()))
        return diff.getDiffEng();
    }
    return null;
  }


  // 첫 문제 난이도 결정 (existDiffStrList = 문제가 존재하는 난이도 리스트)
  public String getFirstProbLevel(Float mastery, List<String> existDiffStrList) {
    if (mastery >= MASTERY_HIGH_THRESHOLD) {
      // high-middle-low
      return getExistLevel(existDiffStrList, Difficulty.getDiffListByOrder(new Integer[] {0,1,2}));

    } else if (mastery >= MASTERY_LOW_THRESHOLD) {
        Float differenceToLow = mastery - MASTERY_LOW_THRESHOLD;
        Float differenceToHigh = MASTERY_HIGH_THRESHOLD - mastery;

        if (differenceToLow < differenceToHigh)
          // middle-low-high
          return getExistLevel(existDiffStrList,  Difficulty.getDiffListByOrder(new Integer[] {1,2,0})); 

        else
          // middle-high-low
          return getExistLevel(existDiffStrList, Difficulty.getDiffListByOrder(new Integer[] {1,0,2}));

    } else {
      // low-middle-high
      return getExistLevel(existDiffStrList, Difficulty.getDiffListByOrder(new Integer[] {2,1,0}));
    }
  }


  // DiffProbListDTO (문제 난이도 별 문제 객체 set DTO) 생성/출력 및 리턴
  public DiffProbListDTO generateDiffProbList(List<Problem> probList) {
    DiffProbListDTO diffProbList = new DiffProbListDTO();

    // 2021-09-02 Added by Sangheon Lee. Shuffle prob list
    Collections.shuffle(probList);

    for (Problem prob : probList) {	diffProbList.addDiffProb(prob, Difficulty.valueOf(prob.getDifficulty())); }
    printDiffProbList(diffProbList);

    return diffProbList;
  }


  // DiffProbListDTO 출력
  public void printDiffProbList(DiffProbListDTO diffProbList) {
    if (PRINT_PROB_INFO) {
      for (Difficulty diff : Difficulty.values()) {
        List<Problem> probList = diffProbList.getDiffProbList(diff);
        log.debug("\t{} level probs = {}", diff.getDiffEng(), getIdListFromProbList(probList));
      }
      log.debug("");
    }
  }


  // 문제 리스트를 최대 크기만큼 slice
  public List<Problem> sliceProbList(List<Problem> probList, Integer MAX_PROBLEM_NUM) {
    return probList.subList(0, Math.min(MAX_PROBLEM_NUM, probList.size()));
  }


  // Problem 객체 리스트에서 probId integer 리스트 추출
  public List<Integer> getIdListFromProbList(List<Problem> probList) {
    return probList.stream().map(prob -> prob.getProbId()).collect(Collectors.toList());
  }


  // 문제 set에 대해, 평균 풀이 예상 시간 측정
  public Integer getSumEstimatedTime(List<Problem> probList) {
    Integer getSumEstimatedTime = 0;

    for (Problem prob : probList) {
      Float probDBTime = prob.getTimeRecommendation();
      Integer probTime = (probDBTime == null || probDBTime == 0.0f) 
                       ? AVERAGE_PROB_ESTIMATED_TIME : Math.round(probDBTime);

      getSumEstimatedTime += probTime;
    }
    return getSumEstimatedTime;
  }


  // 카드 안에 난이도 별로 모든 문제 담기 (adaptive 문제 출제)
  public void addAllProblemSetList(CardDTO card, DiffProbListDTO diffProbList, Integer MIN_PROBLEM_NUM, 
                                   Integer MAX_PROBLEM_NUM) {

    List<ProblemSetListDTO> problemSetList = card.getProbIdSetList();
    Integer estimatedTime = card.getEstimatedTime();

    // 최대 문제 수로 slice
    List<Problem> highProbList = sliceProbList(diffProbList.getHighProbList(), MAX_PROBLEM_NUM);
    List<Problem> middleProbList = sliceProbList(diffProbList.getMiddleProbList(), MAX_PROBLEM_NUM);
    List<Problem> lowProbList = sliceProbList(diffProbList.getLowProbList(), MAX_PROBLEM_NUM);

    // Add problems
    problemSetList.add(ProblemSetListDTO.builder()
                                        .high(getIdListFromProbList(highProbList))
                                        .middle(getIdListFromProbList(middleProbList))
                                        .low(getIdListFromProbList(lowProbList))
                                        .min(MIN_PROBLEM_NUM)
                                        .max(MAX_PROBLEM_NUM)
                                        .build());

    // get estimated time
    Integer avgEstimatedTime = 
        (getSumEstimatedTime(highProbList) + getSumEstimatedTime(middleProbList) + getSumEstimatedTime(lowProbList))
        / (highProbList.size() + middleProbList.size() + lowProbList.size());

    estimatedTime += avgEstimatedTime * MAX_PROBLEM_NUM;
    // add to card object
    card.setEstimatedTime(estimatedTime);
    card.setProbIdSetList(problemSetList);
  }


  // 카드 안에 난이도 확정 & 문제 비율 맞춰 담기 (Adaptive 아닌 경우)
  // probDiffRatio = 현재까지 담은 [상 문제수, 중 문제수, 하 문제수]
  public void addRatioProblemSetList(CardDTO card, DiffProbListDTO diffProbList, Integer MIN_PROB_NUM,
                                     Integer MAX_PROB_NUM, List<Integer> probDiffRatio) {

    List<ProblemSetListDTO> problemSetList = card.getProbIdSetList();
    Integer estimatedTime = card.getEstimatedTime();

    ProblemSetListDTO probSetListDTO = ProblemSetListDTO.builder()
                                                        .min(MIN_PROB_NUM)
                                                        .max(MAX_PROB_NUM)
                                                        .build();

    Integer probIdx = 0;
    // 문제가 많은 순서의 난이도 부터 반복 ex) ["상", "하", "중"]
    for (Difficulty diff : diffProbList.getSizeOrderedDiffList()) {

      List<Problem> probList = diffProbList.getDiffProbList(diff);

      Integer diffIdx = diff.ordinal();
      Integer DIFF_MAX_PROB = diff.getProbNums(); // 미리 정해진, 해당 난이도 문제 개수

      Integer currentProbNum = probDiffRatio.get(diffIdx);			
      if (currentProbNum < DIFF_MAX_PROB) {
        for (int i = 0; i < Math.min(DIFF_MAX_PROB - currentProbNum, probList.size()); i++) {

          if (PRINT_PROB_INFO)
            log.debug("\tAdd probId={} ({})", probList.get(i).getProbId(), diff.getDiffEng());

          // Add problems
          probSetListDTO.addDiffProb(probList.get(i).getProbId(), diff);

          Float probDBTime = probList.get(i).getTimeRecommendation();

          Integer probTime = (probDBTime == null || probDBTime == 0.0f) 
                            ? AVERAGE_PROB_ESTIMATED_TIME 
                            : Math.round(probDBTime);

          estimatedTime += probTime;
          probIdx += 1;
          probDiffRatio.set(diffIdx, probDiffRatio.get(diffIdx) + 1);

          if (probIdx.equals(MAX_PROB_NUM))
            break;
        }
      }
      if (probIdx.equals(MAX_PROB_NUM))
        break;
    }

    // 맞는 난이도가 없으면 난이도 랜덤하게 채우기
    if (!probIdx.equals(MAX_PROB_NUM)) {

      for (Difficulty diff : diffProbList.getSizeOrderedDiffList()) {
        List<Problem> probList = diffProbList.getDiffProbList(diff);

        for (Problem prob : probList) {
          if (!probSetListDTO.getDiffProbIdList(diff).contains(prob.getProbId())) {

            if (PRINT_PROB_INFO)	
              log.debug("\tAdd probId={} ({})", prob.getProbId(), diff.getDiffEng());

            // Add problems
            probSetListDTO.addDiffProb(prob.getProbId(), diff);

            Float probDBTime = prob.getTimeRecommendation();
            
            Integer probTime = (probDBTime == null || probDBTime == 0.0f) 
                              ? AVERAGE_PROB_ESTIMATED_TIME 
                              : Math.round(probDBTime);

            estimatedTime += probTime;
            probIdx += 1;

            if (probIdx.equals(MAX_PROB_NUM))
              break;
          }
        }
        if (probIdx.equals(MAX_PROB_NUM))
          break;
      }
    }

    problemSetList.add(probSetListDTO);
    card.setProbIdSetList(problemSetList);
    card.setEstimatedTime(estimatedTime);
  }


  // 시험/대/중단원 (superCurr) 내에 대/중/소단원 (curr) 별 빈출 유형 수 고려하여 문제 추출
  public void addCurrProblemWithFrequent(CardDTO card, CurrType superCurr, Integer probNum, boolean isAdaptive,
                                         List<Integer> probDiffRatio) {

    List<String> freqOrderedCurrIdList = new ArrayList<>();
    Map<String, Integer> currentCurrProbNumMap = new HashMap<>();
    Map<String, Integer> totalCurrProbNumMap = new HashMap<>();

    // get currId List ordered by count(type, frequent=true)
    switch (superCurr.name()) {
      case "section":
        freqOrderedCurrIdList = problemTypeRepo.findSubSectionIdListInSectionOrderByFreq(superCurr.getCurrId());
        break;

      case "chapter":
        freqOrderedCurrIdList = problemTypeRepo.findSectionIdListInChapterOrderByFreq(superCurr.getCurrId());
        break;

      case "exam":
        freqOrderedCurrIdList = problemTypeRepo.findChapterListInSubSectionSetOrderByFreq(this.examSubSectionIdSet);
        break;

      default:
        throw new RecommendException(RecommendErrorCode.CARD_GENERATOR_ERROR,
                                     String.format("%s is not currType enum.", superCurr.name()));
    }    

    // print
    if (PRINT_CURR_INFO)
      log.info("[{} {}] Curriculums = ", superCurr.name(), superCurr.getCurrId());

    // define prob num for each currId
    int probCnt = 0;		
    Set<String> noProbCurrIdSet = new HashSet<>();

    while (probCnt != probNum) {
      for (String currId : freqOrderedCurrIdList) {

        // currId에 현재 할당된 문제 수
        Integer currProbNum = currentCurrProbNumMap.containsKey(currId) 
                            ? currentCurrProbNumMap.get(currId) : 0;

        // currId 내 제공 가능한 모든 문제 수
        Integer totalCurrProbNum = !totalCurrProbNumMap.containsKey(currId) 
                                 ? problemRepo.findProbCntInCurrId(currId, solvedProbIdSet, this.todayUTC)
                                 : totalCurrProbNumMap.get(currId);

        totalCurrProbNumMap.put(currId, totalCurrProbNum);

        // 포함시킬 문제가 더 있으면 문제 수 + 1
        if (currProbNum < totalCurrProbNum) {
          currentCurrProbNumMap.put(currId, currProbNum + 1);
          probCnt += 1;
        } else { // 문제가 없는 커리큘럼
          noProbCurrIdSet.add(currId);
        }

        if (probCnt == probNum)
          break;
      }

      // break if all currId has no problems
      if (noProbCurrIdSet.size() == freqOrderedCurrIdList.size())
        break;
    }

    CurrMasteryDTO currMastery;
    CurrType curr;
    JsonObject cardDetailJson = new JsonObject(); // 카드 상세 정보

    // 커리큘럼 순서대로 카드 내 currId 문제들 배치
    for (String currId : curriculumRepo.sortByCurrSeq(currentCurrProbNumMap.keySet())) {
      Integer currProbNum = currentCurrProbNumMap.get(currId);

      switch (superCurr.name()) {        
        case "section":
          if (PRINT_CURR_INFO)
            log.info("Sub section {} : {} problems. ", currId, currProbNum);

          addSubSectionProblemWithFrequent(card, currId, currProbNum, isAdaptive, probDiffRatio);

          currMastery = userKnowledgeRepo.findSubSectionMastery(userId, currId);
          cardDetailJson.addProperty(currMastery.getCurrName(), currMastery.getMastery() * 100.0f);

          break;

        case "chapter":
          if (PRINT_CURR_INFO)
            log.info("Section {} : {} problems. ", currId, currProbNum);

          curr = CurrType.valueOf("section");
          curr.setCurrId(currId);

          addCurrProblemWithFrequent(card, curr, currProbNum, isAdaptive, probDiffRatio);

          currMastery = userKnowledgeRepo.findSectionMastery(userId, currId);
          cardDetailJson.addProperty(currMastery.getCurrName(), currMastery.getMastery() * 100.0f);
          break;

        case "exam":
          if (PRINT_CURR_INFO)
            log.info("Chapter {} : {} problems. ", currId, currProbNum);

          curr = CurrType.valueOf("chapter");
          curr.setCurrId(currId);

          addCurrProblemWithFrequent(card, curr, currProbNum, isAdaptive, probDiffRatio);

          currMastery = userKnowledgeRepo.findChapterMastery(userId, currId);
          cardDetailJson.addProperty(currMastery.getCurrName(), currMastery.getMastery() * 100.0f);
          break;

        default:
          throw new RecommendException(RecommendErrorCode.CARD_GENERATOR_ERROR, 
                                       String.format("%s is not currType enum.", superCurr.name()));
      }
    }
    card.setCardDetail(cardDetailJson.toString());
  }


  // 시험/대/중단원 (superCurr) 내에 대/중/소단원 (curr) 별 이해도 고려하여 문제 추출
  public void addCurrProblemWithMastery(CardDTO card, CurrType superCurr, Integer probNum) {

    List<CurrMasteryDTO> currMasteryList = new ArrayList<>();
    Map<String, Integer> currentCurrProbNumMap = new HashMap<>();
    Map<String, Integer> totalCurrProbNumMap = new HashMap<>();
    
    Map<String, Integer> currTypeNumMap = new HashMap<>();

    // get currId List ordered by mastery
    switch (superCurr.name()) {
      case "section":
        currMasteryList = 
          userKnowledgeRepo.findMasteryListInSectionOrderByMastery(userId, superCurr.getCurrId(), this.examSubSectionIdSet);
        break;

      case "chapter":
        currMasteryList = 
          userKnowledgeRepo.findMasteryListInChapterOrderByMastery(userId, superCurr.getCurrId(), this.examSubSectionIdSet);
        break;

      case "exam":
        currMasteryList = 
          userKnowledgeRepo.findMasteryListInSubSectionSetOrderByMastery(userId, this.examSubSectionIdSet);
        break;

      default:
        throw new RecommendException(RecommendErrorCode.CARD_GENERATOR_ERROR, 
                                     String.format("%s is not currType enum.", superCurr.name()));
    }

    // print
    if (PRINT_MASTERY) {
      log.debug("[{} {}] Curriculums = ", superCurr.name(), superCurr.getCurrId());
      for (CurrMasteryDTO currMastery : currMasteryList)
        log.debug("{}\t{}\t(mastery={})", currMastery.getCurrId(), currMastery.getCurrName(), currMastery.getMastery());
    }

    int probCnt = 0;
    Set<String> noProbCurrIdSet = new HashSet<>();
    Set<String> fullCurrIdSet = new HashSet<>();
    JsonObject cardDetailJson = new JsonObject();

    // define prob num for each currId
    Integer probPerType = 1; // 유형마다 포함되는 최대 문제수, 1부터 시작하여 증가시킴 --> 최대한 넓은 범위 커버를 위함
    while (probCnt != probNum) {
      for (CurrMasteryDTO currMastery : currMasteryList) {

        String currId = currMastery.getCurrId();
        String currName = currMastery.getCurrName();
        Float mastery = currMastery.getMastery();

        // 2021-08-24 Guik Jung
        // No Consider if no Problem
        if (noProbCurrIdSet.contains(currId))
          continue;
        Integer typeNum;

        // currId 에 현재 할당된 문제 수
        Integer currProbNum = currentCurrProbNumMap.containsKey(currId) 
                            ? currentCurrProbNumMap.get(currId) : 0;

        // superCurr=section 이면, typeNum이 유효함
        if (superCurr.name().equals("section")) {
          typeNum = !currTypeNumMap.containsKey(currId)
                    ? problemTypeRepo.findTypeCntInSubSection(currId)
                    : currTypeNumMap.get(currId);

          currTypeNumMap.put(currId, typeNum);
        } 
        else {
          typeNum = currProbNum;
        }
    
        if (currProbNum <= typeNum * probPerType) {
          // currId 내 제공 가능한 모든 문제 수
          // 2021-08-19 Guik Jung
          // problemRepo.findProbCntInCurrId -> problemRepo.findExamProbCntInCurrId
          Integer totalCurrProbNum = !totalCurrProbNumMap.containsKey(currId)
                                   ? problemRepo.findExamProbCntInCurrId(currId, solvedProbIdSet, this.todayUTC)
                                   : totalCurrProbNumMap.get(currId);
                                   
          totalCurrProbNumMap.put(currId, totalCurrProbNum);

          // 포함시킬 문제가 더 있으면 문제 수 + 1
          if (currProbNum < totalCurrProbNum) {
            currentCurrProbNumMap.put(currId, currProbNum + 1);
            cardDetailJson.addProperty(currName, mastery * 100.0f);
            probCnt += 1;
          } 
          else // 문제가 없는 커리큘럼
            noProbCurrIdSet.add(currId);
        }        
        else // 유형마다 포함되는 최대 문제수 만큼 꽉차있는 소단원들
          fullCurrIdSet.add(currId);

        if (probCnt == probNum)
          break;
      }

      // break if all currId has no problems
      if (currMasteryList.size() == noProbCurrIdSet.size())
        break;

      // 유형마다 포함되는 최대 문제수 + 1
      // 2021-08-24 Guik Jung
      // 모든 커리큘럼 내의 모든 유형을 채우고도 문제가 남아있을 경우 유형별 문제 추가
      if (currMasteryList.size() == (fullCurrIdSet.size() + noProbCurrIdSet.size())){
        fullCurrIdSet.clear();
        probPerType += 1;
      }
    }
    
    // 커리큘럼 순서대로 카드 내 currId 문제들 배치
    CurrType curr;
    for (String currId : curriculumRepo.sortByCurrSeq(currentCurrProbNumMap.keySet())) {
      Integer currProbNum = currentCurrProbNumMap.get(currId);

      switch (superCurr.name()) {

        case "section":
          if (PRINT_CURR_INFO)
            log.info("Sub section {} : {} problems. ", currId, currProbNum);

          addSubSectionProblemWithMastery(card, currId, currProbNum);
          break;

        case "chapter":
          if (PRINT_CURR_INFO)
            log.info("Section {} : {} problems. ", currId, currProbNum);

          curr = CurrType.valueOf("section");
          curr.setCurrId(currId);

          addCurrProblemWithMastery(card, curr, currProbNum);
          break;

        case "exam":
          if (PRINT_CURR_INFO)
            log.info("Chapter {} : {} problems. ", currId, currProbNum);

          curr = CurrType.valueOf("chapter");
          curr.setCurrId(currId);

          addCurrProblemWithMastery(card, curr, currProbNum);
          break;

        default:
          throw new RecommendException(RecommendErrorCode.CARD_GENERATOR_ERROR, 
                                       String.format("%s is not currType enum.", superCurr.name()));
      }
    }
    card.setCardDetail(cardDetailJson.toString());
  }


  // 소단원 내 문제 출제 모듈. 빈출 유형인 것들 중에서 출제
  public void addSubSectionProblemWithFrequent(CardDTO card, String subSectionId, Integer probNum, boolean isAdaptive,
                                               List<Integer> probDiffRatio) {

    List<Integer> freqTypeIdList = problemTypeRepo.findFreqTypeIdListInSubSection(subSectionId);
    Map<Integer, Integer> currentTypeProbNumMap = new HashMap<>();
    Map<Integer, Integer> totalTypeProbNumMap = new HashMap<>();

    int probCnt = 0;
    Set<Integer> noProbTypeIdSet = new HashSet<>();

    while (probCnt != probNum) {
      for (Integer typeId : freqTypeIdList) {

        // typeId 에 현재 할당된 문제 수
        Integer typeProbNum = currentTypeProbNumMap.containsKey(typeId)
                            ? currentTypeProbNumMap.get(typeId) : 0;

        // typeId 내 제공 가능한 모든 문제 수
        Integer totalTypeProbNum = !totalTypeProbNumMap.containsKey(typeId)
                                 ? problemRepo.findProbCntInType(typeId, solvedProbIdSet, this.todayUTC)
                                 : totalTypeProbNumMap.get(typeId);

        totalTypeProbNumMap.put(typeId, totalTypeProbNum);

        // 포함시킬 문제가 더 있으면 문제 수 + 1
        if (typeProbNum < totalTypeProbNum) {
          currentTypeProbNumMap.put(typeId, typeProbNum + 1);
          probCnt += 1;
        } 
        else { // 문제가 없는 유형
          noProbTypeIdSet.add(typeId);
        }          
        if (probCnt == probNum)
          break;
      }
      if (freqTypeIdList.size() == noProbTypeIdSet.size())
        break;
    }

    // 커리큘럼 순서대로 카드 내 유형 문제들 구성
    for (Integer typeId : problemTypeRepo.sortByTypeSeq(currentTypeProbNumMap.keySet())) {
      Integer typeProbNum = currentTypeProbNumMap.get(typeId);

      if (PRINT_TYPE_ID)
        log.info("\tType {} : {} problems. ", typeId, typeProbNum);

      // 해당 유형 내 모든 문제들을 난이도에 따라 달리 하여 dto 구성
      DiffProbListDTO diffProbList = 
          generateDiffProbList(problemRepo.findProbListByType(typeId, solvedProbIdSet, this.todayUTC));

      // 첫 번째 문제인 경우, 난이도 결정
      if (card.getProbIdSetList().isEmpty()){
        Float firstProbMastery = userKnowledgeRepo.findTypeMastery(userId, typeId).getMastery();
        card.setFirstProbLevel(getFirstProbLevel(firstProbMastery, diffProbList.getExistDiffStrList()));
      }

      // Adaptive 여부에 따라 다른 모듈 call
      if (isAdaptive)
        addAllProblemSetList(card, diffProbList, typeProbNum, typeProbNum);
      else
        addRatioProblemSetList(card, diffProbList, typeProbNum, typeProbNum, probDiffRatio);
    }
  }


  // 소단원 내 문제 출제 모듈. 안본 유형 + 마스터리가 낮은 유형 순서대로 많이 출제
  public void addSubSectionProblemWithMastery(CardDTO card, String subSectionId, Integer probNum) {

    List<TypeMasteryDTO> typeMasteryList = 
      userKnowledgeRepo.findTypeMasteryList(userId, problemTypeRepo.findTypeIdListInSubSection(subSectionId));		

    Map<Integer, Integer> currentTypeProbNumMap = new HashMap<>();
    Map<Integer, Integer> totalTypeProbNumMap = new HashMap<>();

    if (PRINT_MASTERY) {
      log.debug("[subsection {}] Types = ", subSectionId);
      for (TypeMasteryDTO typeMastery : typeMasteryList)
        log.debug("{} (mastery={})", typeMastery.getTypeId(), typeMastery.getMastery());
    }
    
    int probCnt = 0;
    Set<Integer> noProbTypeIdSet = new HashSet<>();

    while (probCnt != probNum) {
      for (TypeMasteryDTO typeMastery : typeMasteryList) {

        Integer typeId = typeMastery.getTypeId();

        // typeId 에 현재 할당된 문제 수
        Integer typeProbNum = currentTypeProbNumMap.containsKey(typeId) 
                            ? currentTypeProbNumMap.get(typeId) : 0;

        // typeId 내 제공 가능한 모든 문제 수
        // 2021-08-19 Guik Jung
        // problemRepo.findProbCntInType -> problemRepo.findExamProbCntInType
        Integer totalTypeProbNum = !totalTypeProbNumMap.containsKey(typeId)
                                 ? problemRepo.findExamProbCntInType(typeId, solvedProbIdSet, this.todayUTC)
                                 : totalTypeProbNumMap.get(typeId);

        totalTypeProbNumMap.put(typeId, totalTypeProbNum);
        
        // 포함시킬 문제가 더 있으면 문제 수 + 1
        if (totalTypeProbNum > typeProbNum) {
          currentTypeProbNumMap.put(typeId, typeProbNum + 1);
          probCnt += 1;
        } 
        else // 문제가 없는 유형
          noProbTypeIdSet.add(typeId);

        if (probCnt == probNum)
          break;
      }
      if (typeMasteryList.size() == noProbTypeIdSet.size())
        break;
    }

    // 커리큘럼 순서대로 카드 내 유형 문제들 구성
    for (Integer typeId : problemTypeRepo.sortByTypeSeq(currentTypeProbNumMap.keySet())) {
      Integer typeProbNum = currentTypeProbNumMap.get(typeId);

      if (PRINT_TYPE_ID)
        log.info("\tType {} : {} problems. ", typeId, typeProbNum);

      // 해당 유형 내 모든 문제들을 난이도에 따라 달리 하여 dto 구성
      // 2021-08-19 Guik Jung
      // problemRepo.findProbListByType -> problemRepo.findExamProbListByType
      DiffProbListDTO diffProbList = 
          generateDiffProbList(problemRepo.findExamProbListByType(typeId, solvedProbIdSet, this.todayUTC));

      // 첫 번째 문제인 경우, 난이도 결정
      if (card.getProbIdSetList().isEmpty()) {
        Float firstProbMastery = userKnowledgeRepo.findTypeMastery(userId, typeId).getMastery();
        card.setFirstProbLevel(getFirstProbLevel(firstProbMastery, diffProbList.getExistDiffStrList()));
      }

      // 항상 adaptive --> 모든 문제들을 난이도 별로 구성
      addAllProblemSetList(card, diffProbList, typeProbNum, typeProbNum);
    }
  }


  // 실력 향상 - 유형 카드
  public CardDTO generateTypeCard(Integer typeId) {

    // 유형 카드 상세 정보
    JsonObject cardDetailJson = new JsonObject();
    Curriculum typeCurriculum = curriculumRepo.findByType(typeId);

    cardDetailJson.addProperty("subSection", typeCurriculum.getSubSection());
    cardDetailJson.addProperty("section", typeCurriculum.getSection());
    cardDetailJson.addProperty("chapter", typeCurriculum.getChapter());

    // 유형 점수
    TypeMasteryDTO typeMastery = userKnowledgeRepo.findTypeMastery(userId, typeId);
    Float mastery = typeMastery != null ? typeMastery.getMastery() : 0.7429f;
    
    CardDTO typeCard = CardDTO.builder()
                                  .cardType(TYPE_CARD_TYPESTR)
                                  .cardTitle(problemTypeRepo.findTypeNameById(typeId))
                                  .probIdSetList(new ArrayList<>())
                                  .estimatedTime(0)
                                  .cardDetail(cardDetailJson.toString())
                                  .cardScore(mastery * 100)
                                  .build();
                                  
    // 해당 유형 내 모든 문제들을 난이도에 따라 달리 하여 dto 구성
    DiffProbListDTO diffProbList = 
      generateDiffProbList(problemRepo.findProbListByType(typeId, solvedProbIdSet, this.todayUTC));

    addAllProblemSetList(typeCard, diffProbList, MIN_TYPE_CARD_PROB_NUM, MAX_TYPE_CARD_PROB_NUM);
    typeCard.setFirstProbLevel(getFirstProbLevel(mastery, diffProbList.getExistDiffStrList()));
    return typeCard;
  }


  // 실력 향상 - 중간 평가 카드 (type="section"/"chapter")
  public CardDTO generateTestCard(String curriculumId, String type, String cardType) {

    CurrMasteryDTO mastery = type.equals("section")
                            ? userKnowledgeRepo.findSectionMastery(userId, curriculumId)
                            : userKnowledgeRepo.findChapterMastery(userId, curriculumId);

    log.info("SECTION_TEST card: {}, {}, {}", mastery.getCurrId(), mastery.getCurrName(), mastery.getMastery());
    List<Integer> probDiffRatio = new ArrayList<>(Arrays.asList(0, 0, 0));

    CardDTO testCard = CardDTO.builder()
                                  .cardType(cardType)
                                  .cardTitle(mastery.getCurrName())
                                  .probIdSetList(new ArrayList<>())
                                  .estimatedTime(0)
                                  .cardScore(mastery.getMastery() * 100)
                                  .build();

    CurrType currType = CurrType.valueOf(type);
    currType.setCurrId(curriculumId);

    addCurrProblemWithFrequent(testCard, currType, MAX_CARD_PROB_NUM, false, probDiffRatio);

    return testCard;
  }


  // 실력 향상 - 보충 카드
  public CardDTO generateSupplementCard(List<TypeMasteryDTO> typeMasteryList) {

    CardDTO supplementCard = CardDTO.builder()
                                        .cardType(SUPPLE_CARD_TYPESTR)
                                        .cardTitle(String.format(SUPPLE_CARD_TITLE_FORMAT, typeMasteryList.size()))
                                        .probIdSetList(new ArrayList<>())
                                        .estimatedTime(0)
                                        .build();
    
    int cnt = 1;
    JsonObject cardDetailJson = new JsonObject();

    for (TypeMasteryDTO typeMastery : typeMasteryList) {

      Integer typeId = typeMastery.getTypeId();
      Float mastery = typeMastery.getMastery();

      cardDetailJson.addProperty(problemTypeRepo.findTypeNameById(typeId), mastery * 100.0f);

      log.info("{}th type = {} (mastery={}) with {} problems. ", cnt, typeId, mastery, SUPPLE_CARD_PROB_NUM_PER_TYPE);

      // 해당 유형 내 모든 문제들을 난이도에 따라 달리 하여 dto 구성
      DiffProbListDTO diffProbList = generateDiffProbList(problemRepo.findProbListByType(typeId, null, this.todayUTC));
      addAllProblemSetList(supplementCard, diffProbList, SUPPLE_CARD_PROB_NUM_PER_TYPE, SUPPLE_CARD_PROB_NUM_PER_TYPE);

      if (cnt == 1)
        supplementCard.setFirstProbLevel(getFirstProbLevel(mastery, diffProbList.getExistDiffStrList()));

      cnt += 1;
    }    

    supplementCard.setCardDetail(cardDetailJson.toString());
    return supplementCard;
  }


  // 실력 향상 - 추가 보충카드
  public CardDTO generateAddtlSupplementCard(List<TypeMasteryDTO> typeMasteryList) {

    CardDTO supplementCard = CardDTO.builder()
                                        .cardType(ADDTL_SUPPLE_CARD_TYPESTR)
                                        .cardTitle(String.format(ADDTL_SUPPLE_CARD_TITLE_FORMAT, typeMasteryList.size()))
                                        .probIdSetList(new ArrayList<>())
                                        .estimatedTime(0)
                                        .build();
    
    int cnt = 1;																		
    JsonObject cardDetailJson = new JsonObject();

    for (TypeMasteryDTO typeMastery : typeMasteryList) {

      Integer typeId = typeMastery.getTypeId();
      Float mastery = typeMastery.getMastery();

      cardDetailJson.addProperty(problemTypeRepo.findTypeNameById(typeId), mastery * 100.0f);

      log.info("{}th type = {} (mastery={}) with {} problems. ", cnt, typeId, mastery, SUPPLE_CARD_PROB_NUM_PER_TYPE);

      // 해당 유형 내 모든 문제들을 난이도에 따라 달리 하여 dto 구성
      DiffProbListDTO diffProbList = generateDiffProbList(problemRepo.findProbListByType(typeId, null, this.todayUTC));
      addAllProblemSetList(supplementCard, diffProbList, SUPPLE_CARD_PROB_NUM_PER_TYPE, SUPPLE_CARD_PROB_NUM_PER_TYPE);

      if (cnt == 1)
        supplementCard.setFirstProbLevel(getFirstProbLevel(mastery, diffProbList.getExistDiffStrList()));

      cnt += 1;
    }

    supplementCard.setCardDetail(cardDetailJson.toString());
    return supplementCard;
  }


  // 시험 대비 - section_exam (한 중단원 범위 내 20개), full_scope_exam (중단원 내 4-5개) 카드
  public CardDTO generateExamCard(String curriculumId, String cardType, Integer probNum) {

    // 카드 상세 정보
    JsonObject cardDetailJson = new JsonObject();
    Curriculum sectionCurriculum = curriculumRepo.findById(curriculumId).orElse(new Curriculum());
    cardDetailJson.addProperty("chapter", sectionCurriculum.getChapter());

    CurrMasteryDTO mastery = userKnowledgeRepo.findSectionMastery(userId, curriculumId);
    log.info("{}, {}, {}", mastery.getCurrId(), mastery.getCurrName(), mastery.getMastery());

    CardDTO examCard = CardDTO.builder()
                                  .cardType(cardType)
                                  .cardTitle(mastery.getCurrName())
                                  .probIdSetList(new ArrayList<>())
                                  .estimatedTime(0)
                                  .cardScore(mastery.getMastery() * 100)
                                  .cardDetail(cardDetailJson.toString())
                                  .build();

    CurrType currType = CurrType.valueOf("section");
    currType.setCurrId(curriculumId);

    addCurrProblemWithMastery(examCard, currType, probNum);

    return examCard;
  }
  

  // 시험 대비 - 모의고사 카드
  public CardDTO generateTrialExamCard(String examKeyword) {

    String[] trialExamInfo = examKeyword.split("-");
    String examType = trialExamInfo[2].equals("mid") ? "중간고사" : "기말고사";
    
    CurrMasteryDTO mastery = userKnowledgeRepo.findExamMastery(userId, this.examSubSectionIdSet);

    CardDTO trialExamCard = CardDTO.builder()
                                       .cardType(TRIAL_EXAM_CARD_TYPESTR)
                                       .cardTitle(String.format(TRIAL_EXAM_CARD_TITLE_FORMAT, trialExamInfo[0],
                                                                 trialExamInfo[1], examType))
                                       .probIdSetList(new ArrayList<>())
                                       .estimatedTime(0)
                                       .cardScore(mastery.getMastery() * 100)
                                       .firstProbLevel("middle")
                                       .build();

    CurrType currType = CurrType.valueOf("exam");
    currType.setCurrId("모의고사");

    addCurrProblemWithMastery(trialExamCard, currType, MAX_CARD_PROB_NUM);

    return trialExamCard;
  }

  
  public CardDTO generateCard(CardConfigDTO cardConfig) {

    CardDTO card = null;
    switch (cardConfig.getCardType()) {

      case TYPE_CARD_TYPESTR:
        log.info("------ {} card (type {})", cardConfig.getCardType(), cardConfig.getTypeId());
        card = generateTypeCard(cardConfig.getTypeId());
        break;

      case SUPPLE_CARD_TYPESTR:
        log.info("------ {} card", cardConfig.getCardType());
        card = generateSupplementCard(cardConfig.getTypeMasteryList());
        break;

      case SECTION_TEST_CARD_TYPESTR:
        log.info("------ {} card ({})", cardConfig.getCardType(), cardConfig.getCurriculumId());
        card = generateTestCard(cardConfig.getCurriculumId(), "section", cardConfig.getCardType());
        break;

      case ADDTL_SUPPLE_CARD_TYPESTR:
        log.info("------ {} card", cardConfig.getCardType());
        card = generateAddtlSupplementCard(cardConfig.getTypeMasteryList());
        break;

      case SECTION_EXAM_CARD_TYPESTR:
      case FULL_SCOPE_EXAM_CARD_TYPESTR:
        log.info("------ {} card ({} {} problems)", cardConfig.getCardType(), cardConfig.getCurriculumId(), cardConfig.getProbNum());
        card = generateExamCard(cardConfig.getCurriculumId(), cardConfig.getCardType(), cardConfig.getProbNum());
        break;

      case TRIAL_EXAM_CARD_TYPESTR:
        log.info("------ {} card ({})", cardConfig.getCardType(), cardConfig.getExamKeyword());
        card = generateTrialExamCard(cardConfig.getExamKeyword());
        break;

      default:
        throw new RecommendException(RecommendErrorCode.CARD_GENERATOR_ERROR, 
                                     "Invalid card config type : " + cardConfig.getCardType());

    }

    return card;
  }
}
