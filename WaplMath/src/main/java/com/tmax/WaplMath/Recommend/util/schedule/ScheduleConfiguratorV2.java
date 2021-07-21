package com.tmax.WaplMath.Recommend.util.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserExamScope;
import com.tmax.WaplMath.Common.repository.user.UserExamScopeRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.config.CardConstants;
import com.tmax.WaplMath.Recommend.util.history.ScheduleHistoryManagerV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedule card configuration logic v2
 * @author Sangheon_lee
 * @since 2021-06-30
 */
@Slf4j
@Component
public class ScheduleConfiguratorV2 extends CardConstants {

  // Repository
  @Autowired
  @Qualifier("RE-ProblemRepo")
  private ProblemRepo problemRepo;

  @Autowired
  private UserRepo userRepo;

  @Autowired
  @Qualifier("RE-ProblemTypeRepo")
  private ProblemTypeRepo problemTypeRepo;

  @Autowired
  private UserExamScopeRepo userExamScopeRepo;

  @Autowired
  @Qualifier("RE-CurriculumRepo")
  private CurriculumRepo curriculumRepo;

  @Autowired
  @Qualifier("RE-UserKnowledgeRepo")
  private UserKnowledgeRepo userKnowledgeRepo;

  @Autowired
  ScheduleHistoryManagerV1 historyManager;


  private @Setter String userId;

  private String today;

  private @Getter Set<Integer> solvedProbIdSet;

  private @Getter Set<String> examSubSectionIdSet;


  public void setUserValue(String userId){
    // set today
    this.userId = userId;

    // this.today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    ZoneId zoneId = ZoneId.of("Asia/Seoul");
    this.today = ZonedDateTime.now(zoneId).plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    setUserSolvedProbIdSet(userId);
    this.examSubSectionIdSet = new HashSet<>();
  }


  public User getValidUserInfo(String userId) {

    // Check whether userId is in USER_MASTER TB
    User userInfo = userRepo.findById(userId)
        .orElseThrow(() -> new RecommendException(RecommendErrorCode.USER_NOT_EXIST_ERROR, userId));

    // Check whether user exam information is null
    if (userInfo.getGrade() == null 		|| userInfo.getSemester() == null || 
        userInfo.getExamType() == null 	|| userInfo.getCurrentCurriculumId() == null) {

      log.error("User info null error: {}, {}, {}, {}", userInfo.getGrade(), 
                                                        userInfo.getSemester(),
                                                        userInfo.getExamType(), 
                                                        userInfo.getCurrentCurriculumId());

      throw new RecommendException(RecommendErrorCode.USER_INFO_NULL_ERROR, 
                                   "Call /userbasicinfo PUT service first. ");
    }
    return userInfo;
  }


  public UserExamScope getValidUserExamInfo(String userId) {

    // Check whether userId is in USER_MASTER TB
    UserExamScope userExamInfo = userExamScopeRepo.findById(userId)
        .orElseThrow(() -> new RecommendException(RecommendErrorCode.USER_NOT_EXIST_ERROR, userId));

    // Check whether user exam information is null
    if (userExamInfo.getUser().getExamStartDate() == null || userExamInfo.getUser().getExamDueDate() == null || 
        userExamInfo.getStartSubSectionId() == null 			|| userExamInfo.getEndSubSectionId() == null) {
          
      log.error("User exam info null error: {}, {}, {}, {}", userExamInfo.getUser().getExamStartDate(), 
                                                             userExamInfo.getUser().getExamDueDate(),
                                                             userExamInfo.getStartSubSectionId(), 
                                                             userExamInfo.getEndSubSectionId());

      throw new RecommendException(RecommendErrorCode.USER_INFO_NULL_ERROR, 
                                   "Call /userexaminfo PUT service first. ");
    }
    return userExamInfo;
  }


  public void setUserSolvedProbIdSet(String userId){		
    // Get solved problem set
    List<String> sourceTypeList =
        new ArrayList<>(Arrays.asList(TYPE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      SUPPLE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      SECTION_TEST_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      CHAPTER_TEST_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      TRIAL_EXAM_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX));

    this.solvedProbIdSet = historyManager.getSolvedProbIdSet(userId, today, "", sourceTypeList);
  }


  public void setExamSubSectionIdSet(UserExamScope userExamScopeInfo) {		
    // sub-section lis in exam
    List<String> examSubSectionIdList = userExamScopeInfo.getExceptSubSectionIdList() != null

        ? curriculumRepo.findSubSectionListBetweenExcept(userExamScopeInfo.getStartSubSectionId(),
                                                         userExamScopeInfo.getEndSubSectionId(),
                                                         Arrays.asList(userExamScopeInfo.getExceptSubSectionIdList().split(", "))) 

        : curriculumRepo.findSubSectionListBetween(userExamScopeInfo.getStartSubSectionId(),
                                                   userExamScopeInfo.getEndSubSectionId());

    this.examSubSectionIdSet = examSubSectionIdList.stream().collect(Collectors.toSet());

    log.info("Excepted sub section list = {}", userExamScopeInfo.getExceptSubSectionIdList());
    log.info("Total sub section list in exam = {}", this.examSubSectionIdSet);
  }


  public List<String> getNormalSubSectionList(String userId) {

    User userInfo = getValidUserInfo(userId);

    // 이번 학기 마지막까지
    String endCurriculumId = ExamScope.examScope.get(userInfo.getGrade() + "-" + userInfo.getSemester() + "-" + "final").get(1);

    return curriculumRepo.findSubSectionListBetween(userInfo.getCurrentCurriculumId(), endCurriculumId);

  }


  public String getUserExamKeyword(String userId) {

    User userInfo = getValidUserInfo(userId);

    return String.format("%s-%s-%s", userInfo.getGrade(), userInfo.getSemester(), userInfo.getExamType());

  }


  public void printTypeMasteryList(List<TypeMasteryDTO> typeMasteryList) {		

    for (TypeMasteryDTO typeMastery : typeMasteryList)		
      log.info("\tTypeId = {}, mastery = {}", typeMastery.getTypeId(), typeMastery.getMastery());
  }


  public ScheduleConfigDTO getNormalScheduleConfig() {
    
    // schedule config list
    List<CardConfigDTO> cardConfigList = new ArrayList<>();
    Set<String> addtlSubSectionIdSet = new HashSet<>();
    
    Integer totalCardProbNum = 0;

    // 학년 학기 내 소단원 목록들
    List<String> subSectionIdList = getNormalSubSectionList(userId);
    log.info("Total sub section list = {}", subSectionIdList);

    // 중간평가 판단 - 중단원만 일단
    Set<String> sectionIdSet = subSectionIdList.stream()
                                               .map(subSection -> subSection.substring(0, 14))
                                               .collect(Collectors.toSet());
    log.info("1. Total section list : {}", sectionIdSet);

    List<Integer> completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "",
            TYPE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("2. Types already solved : {}", completedTypeIdList);

    List<ProblemType> remainTypeList = problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, completedTypeIdList);
     // 안푼 중단원들
    Set<String> notDoneSectionIdSet = completedTypeIdList.isEmpty() 
                                      ? new HashSet<>(sectionIdSet) 
                                      : remainTypeList.stream()
                                                      .map(type -> type.getCurriculumId().substring(0, 14))
                                                      .collect(Collectors.toSet());
    log.info("3. Sections not solved : {}", notDoneSectionIdSet);

    sectionIdSet.removeAll(notDoneSectionIdSet); // sectionSet에 완벽히 푼 단원들 저장됨
    log.info("4. Sections completely solved through TYPE cards : {}", sectionIdSet);

    Set<String> completedSectionIdSet = historyManager.getCompletedSectionIdList(userId, today, 
            SECTION_TEST_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("5. Sections already solved through SECTION_TEST cards : {}", completedSectionIdSet);

    sectionIdSet.removeAll(completedSectionIdSet); // sectionSet에 완벽히 푼 단원들 - 이미 푼 중간평가 저장됨
    log.info("6. Candidate sections for the SECTION_TEST card : {}", sectionIdSet);

    // 완벽히 푼 단원이 있으면 중간 평가
    if (!sectionIdSet.isEmpty()) {

      String sectionId = sectionIdSet.iterator().next();
      log.info("\tSECTION_TEST card. : {}", sectionId);

      cardConfigList.add(CardConfigDTO.builder()
                                      .cardType(SECTION_TEST_CARD_TYPESTR)
                                      .curriculumId(sectionId)
                                      .build());

      addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
      
      return ScheduleConfigDTO.builder()
                              .cardConfigList(cardConfigList)
                              .addtlSubSectionIdSet(addtlSubSectionIdSet)
                              .build();
    }

    // 보충 필요한지 판단
    List<Integer> suppleTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", 
            SUPPLE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("7. Type list already solved through SUPPLE cards = {}", suppleTypeIdList);

    List<Integer> solvedTypeIdList = historyManager.getCompletedTypeIdListAfterSuppleCard(userId, today);

    solvedTypeIdList.removeAll(suppleTypeIdList);

    if (!solvedTypeIdList.isEmpty()) {
      List<TypeMasteryDTO> lowTypeMasteryList 
          = userKnowledgeRepo.findNLowTypeMasteryList(userId, solvedTypeIdList, MASTERY_LOW_THRESHOLD);

      log.info("8. Low type mastery list except solved through SUPPLE cards = ");
      printTypeMasteryList(lowTypeMasteryList);

      // 보충 채울만큼 넉넉하면 보충 카드
      if (lowTypeMasteryList.size() >= SUPPLE_CARD_TYPE_NUM) {
        log.info("\tSUPPLE card (1).");

        List<TypeMasteryDTO> typeMasteryList = lowTypeMasteryList.subList(0, SUPPLE_CARD_TYPE_NUM);

        List<Integer> suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());

        cardConfigList.add(CardConfigDTO.builder()
                                        .cardType(SUPPLE_CARD_TYPESTR)
                                        .typeMasteryList(typeMasteryList)
                                        .build());

        addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
        totalCardProbNum += SUPPLE_CARD_TYPE_NUM * SUPPLE_CARD_PROB_NUM_PER_TYPE;

        if (lowTypeMasteryList.size() >= SUPPLE_CARD_TYPE_NUM * 2) {
          log.info("\tSUPPLE card (2). ");

          typeMasteryList = lowTypeMasteryList.subList(SUPPLE_CARD_TYPE_NUM, SUPPLE_CARD_TYPE_NUM * 2);

          suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());

          cardConfigList.add(CardConfigDTO.builder()
                                          .cardType(SUPPLE_CARD_TYPESTR)
                                          .typeMasteryList(typeMasteryList)
                                          .build());

          addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
          totalCardProbNum += SUPPLE_CARD_TYPE_NUM * SUPPLE_CARD_PROB_NUM_PER_TYPE;
        }
      }

      // 많이 풀었는데 보충이 안나타나도 보충 카드
      else if (solvedTypeIdList.size() >= SUPPLE_CARD_TYPE_THRESHOLD) {
        log.info("\t{} types are high mastery --> SUPPLE card.", SUPPLE_CARD_TYPE_THRESHOLD);

        List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findBottomTypeMasteryList(userId, solvedTypeIdList,
          SUPPLE_CARD_TYPE_NUM);

        List<Integer> suppleCardTypeIdList = typeMasteryList.stream()
                                                            .map(type -> type.getTypeId())
                                                            .collect(Collectors.toList());

        cardConfigList.add(CardConfigDTO.builder()
                                        .cardType(SUPPLE_CARD_TYPESTR)
                                        .typeMasteryList(typeMasteryList)
                                        .build());

        addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
        totalCardProbNum += SUPPLE_CARD_TYPE_NUM * SUPPLE_CARD_PROB_NUM_PER_TYPE;
      }
    }
    // 나머지 카드들 유형카드로 채우기
    List<Integer> noProbTypeIdList = new ArrayList<>();
    // 공부 안한 유형uk가 있으면 유형 카드
    if (!remainTypeList.isEmpty()) {

      for (ProblemType type : remainTypeList) {

        if (problemRepo.NfindProbListByType(type.getTypeId(), solvedProbIdSet).isEmpty()) {
          noProbTypeIdList.add(type.getTypeId());
          continue;
        }

        log.info("TYPE card : {} ", type.getTypeId());

        cardConfigList.add(CardConfigDTO.builder()
                                        .cardType(TYPE_CARD_TYPESTR)
                                        .typeId(type.getTypeId())
                                        .build());

        addtlSubSectionIdSet.add(problemTypeRepo.findById(type.getTypeId())
                                                .orElse(new ProblemType())
                                                .getCurriculumId());

        totalCardProbNum += MAX_TYPE_CARD_PROB_NUM;

        if (totalCardProbNum >= MAX_CARD_PROB_NUM)
          break;
      }
      log.info("Types couldn't make cards (no problems) : {} ", noProbTypeIdList);
    }
    // 유형도 다풀었는데 문제가 안채워지면 추가 보충카드
    if (totalCardProbNum < MAX_CARD_PROB_NUM) {
      String examKeyword = getUserExamKeyword(userId);

      Integer addtiTypeNum = (int) Math.ceil((MAX_CARD_PROB_NUM - totalCardProbNum) / 2.0);
      log.info("ADDTL_SUPPLE card with {} problems. ", addtiTypeNum);

      List<TypeMasteryDTO> addtiTypeMasteryList 
            = userKnowledgeRepo.findTypeMasteryListBetween(userId, 
                                                            ExamScope.examScope.get(examKeyword).get(0),
                                                            ExamScope.examScope.get(examKeyword).get(1))
                               .subList(0, addtiTypeNum);

      cardConfigList.add(CardConfigDTO.builder()
                                      .cardType(ADDTL_SUPPLE_CARD_TYPESTR)
                                      .typeMasteryList(addtiTypeMasteryList)
                                      .build());

      List<Integer> addtlSuppleCardTypeIdList = addtiTypeMasteryList.stream()
                                                                    .map(type -> type.getTypeId())
                                                                    .collect(Collectors.toList());

      addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));
    }

    return ScheduleConfigDTO.builder()
                            .cardConfigList(cardConfigList)
                            .addtlSubSectionIdSet(addtlSubSectionIdSet)
                            .build();
  }


  // set to dummy --> 카드 종류별로 return
  public ScheduleConfigDTO getDummyScheduleConfig() {

    // schedule config list
    List<CardConfigDTO> cardConfigList = new ArrayList<>();
    Set<String> addtlSubSectionIdSet = new HashSet<>();

    UserExamScope userExamScopeInfo = getValidUserExamInfo(userId);

    // get section id, remain type list
    String sectionId = getValidUserInfo(userId).getCurrentCurriculumId().substring(0, 14);
    List<ProblemType> typeList = problemTypeRepo.NfindRemainTypeIdList(getNormalSubSectionList(userId), null);

    // 유형카드 : 첫 번째 유형
    Integer typeId = typeList.get(0).getTypeId();		
    log.info("TYPE card : {}", typeId);

    cardConfigList.add(CardConfigDTO.builder()
                                    .cardType(TYPE_CARD_TYPESTR)
                                    .typeId(typeId)
                                    .build());

    addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId)
                                            .orElse(new ProblemType())
                                            .getCurriculumId());

    // 보충 카드 : 둘, 셋, 네번째 유형에 대해
    List<Integer> solvedTypeIdList = new ArrayList<>(Arrays.asList(typeList.get(1).getTypeId(), 
                                                                   typeList.get(2).getTypeId(), 
                                                                   typeList.get(3).getTypeId()));

    List<TypeMasteryDTO> lowMasteryTypeList =
        userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, new ArrayList<>(), 1.0f);

    log.info("Low type mastery list except solved through SUPPLE cards = ");
    printTypeMasteryList(lowMasteryTypeList);
    
    List<Integer> suppleCardTypeIdList = lowMasteryTypeList.stream()
                                                           .map(type -> type.getTypeId())
                                                           .collect(Collectors.toList());

    cardConfigList.add(CardConfigDTO.builder()
                                    .cardType(SUPPLE_CARD_TYPESTR)
                                    .typeMasteryList(lowMasteryTypeList)
                                    .build());

    addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
    
    // 중간 평가 카드 : 현재 중단원
    log.info("SECTION_TEST card : {}", sectionId);

    cardConfigList.add(CardConfigDTO.builder()
                                    .cardType(SECTION_TEST_CARD_TYPESTR)
                                    .curriculumId(sectionId)
                                    .build());

    addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));

    // 추가 보충 카드
    Integer addtiTypeNum = (int) Math.ceil((MAX_CARD_PROB_NUM - 4) / 2.0);
    log.info("ADDTL_SUPPLE card with {} problems. ", addtiTypeNum);

    List<TypeMasteryDTO> addtiTypeMasteryList = 
      userKnowledgeRepo.findTypeMasteryListBetween(userId,
              userExamScopeInfo.getStartSubSectionId(), userExamScopeInfo.getEndSubSectionId())
                       .subList(0, addtiTypeNum);

    List<Integer> addtlSuppleCardTypeIdList = addtiTypeMasteryList.stream()
                                                                  .map(type -> type.getTypeId())
                                                                  .collect(Collectors.toList());
    
    cardConfigList.add(CardConfigDTO.builder()
                                    .cardType(ADDTL_SUPPLE_CARD_TYPESTR)
                                    .typeMasteryList(addtiTypeMasteryList)
                                    .build());

    addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));

    // 모의고사 카드
    String userExamKeyword = getUserExamKeyword(userId);
    log.info("TRIAL_EXAM card : {} ", userExamKeyword);

    cardConfigList.add(CardConfigDTO.builder()
                                    .cardType(TRIAL_EXAM_CARD_TYPESTR)
                                    .examKeyword(userExamKeyword)
                                    .build());

    addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);

    return ScheduleConfigDTO.builder()
                            .cardConfigList(cardConfigList)
                            .addtlSubSectionIdSet(addtlSubSectionIdSet)
                            .build();
  }


  public ScheduleConfigDTO getExamScheduleConfig() {

    // schedule config list
    List<CardConfigDTO> cardConfigList = new ArrayList<>();
    Set<String> addtlSubSectionIdSet = new HashSet<>();

    // get user exam info
    UserExamScope userExamScopeInfo = getValidUserExamInfo(userId);
    setExamSubSectionIdSet(userExamScopeInfo);

    // check days to prepare exam
    Integer totalDays = (int) userExamScopeInfo.getUser().getExamStartDate().toLocalDateTime()
        .until(userExamScopeInfo.getUser().getExamDueDate().toLocalDateTime(), ChronoUnit.DAYS);
        
    Integer remainDays = (int) LocalDateTime.now()
        .until(userExamScopeInfo.getUser().getExamDueDate().toLocalDateTime(), ChronoUnit.DAYS);
        
    log.info("Total exam preparation days = {}, remain days = {}", totalDays, remainDays);

    // 시험 범위 중단원
    Set<String> sectionIdSet = this.examSubSectionIdSet.stream()
                                                       .map(subSection -> subSection.substring(0, 14))
                                                       .collect(Collectors.toSet());
    log.info("1. Sections in exam : {} ", sectionIdSet);

    // Decide num of trial exam cards
    Integer numTrialExamCards = totalDays <= sectionIdSet.size() + 1 ? 1 : 2;
    log.info("Num of TRIAL_EXAM cards = {}", numTrialExamCards);

    // 남은 일수가 적을 때 모의고사 카드 제공
    if(remainDays <= numTrialExamCards) {

      String userExamKeyword = getUserExamKeyword(userId);
      log.info("\tTRIAL_EXAM card : {} ", userExamKeyword);

      cardConfigList.add(CardConfigDTO.builder()
                                      .cardType(TRIAL_EXAM_CARD_TYPESTR)
                                      .examKeyword(userExamKeyword)
                                      .build());

      addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);
      
      return ScheduleConfigDTO.builder()
                              .cardConfigList(cardConfigList)
                              .addtlSubSectionIdSet(addtlSubSectionIdSet)
                              .build();
    }

    // FULL_SCOPE_EXAM 카드 제공
    if(totalDays <= sectionIdSet.size()) {
      log.info("\tTotal {} days, {} sections --> FULL_SCOPE_EXAM card.", totalDays, sectionIdSet.size());
      Integer fullScopeCardProbNum = (int) Math.ceil((double) MAX_CARD_PROB_NUM / sectionIdSet.size());

      for (String sectionId: sectionIdSet) {
        log.info("FULL_SCOPE_EXAM card (Sections : {}, Probs : {}) ", sectionId, fullScopeCardProbNum);

        cardConfigList.add(CardConfigDTO.builder()
                                        .cardType(FULL_SCOPE_EXAM_CARD_TYPESTR)
                                        .curriculumId(sectionId)
                                        .probNum(fullScopeCardProbNum)
                                        .build());
      }

      addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSectionSet(sectionIdSet));
  
      return ScheduleConfigDTO.builder()
                              .cardConfigList(cardConfigList)
                              .addtlSubSectionIdSet(addtlSubSectionIdSet)
                              .build();
    }

    // SECTION_EXAM 카드 제공되는 개수
    Integer numSectionExamCards = Math.floorDiv(totalDays - numTrialExamCards, sectionIdSet.size());

    Map<String, Integer> sectionExamCardsNum = new HashMap<>();
    
    sectionIdSet.forEach(sectionId -> sectionExamCardsNum.put(sectionId, numSectionExamCards));

    log.info("2. Number of SECTION_EXAM cards per section : {} ", sectionExamCardsNum);

    Map<String, Integer> completedSectionExamCardsNum = historyManager.getCompletedSectionNum(
        userId, today, SECTION_EXAM_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);

    log.info("3. Number of SECTION_EXAM cards already solved : {} ", completedSectionExamCardsNum);

    for (Map.Entry<String, Integer> entry : sectionExamCardsNum.entrySet()) {
      if (completedSectionExamCardsNum.containsKey(entry.getKey())) {
        Integer remainCardCnt = entry.getValue() - completedSectionExamCardsNum.get(entry.getKey());

        if (remainCardCnt <= 0)
          sectionExamCardsNum.remove(entry.getKey());

        else
          sectionExamCardsNum.put(entry.getKey(), remainCardCnt);
      }
    }

    log.info("4. Number of SECTION_EXAM cards to be offered in future : {} ", sectionExamCardsNum);

    if (!sectionExamCardsNum.keySet().isEmpty()) {
      // SECTION_EXAM 카드 제공
      String sectionId = curriculumRepo.sortByCurrSeq(sectionExamCardsNum.keySet()).get(0);
      log.info("SECTION_EXAM card (Section : {})", sectionId);

      cardConfigList.add(CardConfigDTO.builder()
                                      .cardType(SECTION_EXAM_CARD_TYPESTR)
                                      .curriculumId(sectionId)
                                      .probNum(MAX_CARD_PROB_NUM)
                                      .build());

      addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
      
      return ScheduleConfigDTO.builder()
                              .cardConfigList(cardConfigList)
                              .addtlSubSectionIdSet(addtlSubSectionIdSet)
                              .build();
    }

    // FULL_SCOPE_EXAM 카드 제공
    Integer fullScopeCardProbNum = (int) Math.ceil((double) MAX_CARD_PROB_NUM / sectionIdSet.size());

    for	(String sectionId: sectionIdSet) {
      log.info("FULL_SCOPE_EXAM card in remain days (Sections : {}, Probs : {}) ", sectionId, fullScopeCardProbNum);
      
      cardConfigList.add(CardConfigDTO.builder()
                                      .cardType(FULL_SCOPE_EXAM_CARD_TYPESTR)
                                      .curriculumId(sectionId)
                                      .probNum(fullScopeCardProbNum)
                                      .build());
    }

    addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSectionSet(sectionIdSet));
    
    return ScheduleConfigDTO.builder()
                            .cardConfigList(cardConfigList)
                            .addtlSubSectionIdSet(addtlSubSectionIdSet)
                            .build();
  }
}
