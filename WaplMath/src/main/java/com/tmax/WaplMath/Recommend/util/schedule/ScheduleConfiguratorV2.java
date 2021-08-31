package com.tmax.WaplMath.Recommend.util.schedule;

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

  // user info vars
  private String userId;
  private String today;
  private @Getter Set<Integer> solvedProbIdSet; // 이미 푼 문제 Id set
  private @Getter Set<String> examSubSectionIdSet; // 시험 범위 내 소단원 Id set

  // return vars
  private List<CardConfigDTO> cardConfigList;
  private Set<String> addtlSubSectionIdSet; // 완벽 학습을 위한 소단원 Id set (현재 사용 안함)

  // intermediate vars
  private Integer totalProbNum;  // config 내 문제 개수 합 (실력 향상)
  private List<ProblemType> remainTypeList; // 앞으로 풀 유형들 (실력 향상)
  private Set<String> validSectionIdSet; // 제공할 문제가 있는 중단원 Id set (시험 대비)


  // initialize configurator variables
  public void initConfigurator(String userId){
    // set today local date
    this.today = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                              .plusDays(1)
                              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
    // set user info vars
    this.userId = userId;
    this.solvedProbIdSet = getSolvedProbIdSet(userId);
    this.examSubSectionIdSet = new HashSet<>();

    // init return vars
    this.cardConfigList = new ArrayList<>();
    this.addtlSubSectionIdSet = new HashSet<>();

    // init intermediate vars
    this.totalProbNum = 0;
    this.remainTypeList = new ArrayList<>();
    this.validSectionIdSet = new HashSet<>();
  }


  // 사용자가 이미 푼 문제 Id 리턴
  public Set<Integer> getSolvedProbIdSet(String userId){
    List<String> sourceTypeList =
        new ArrayList<>(Arrays.asList(TYPE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      SUPPLE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      SECTION_TEST_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      CHAPTER_TEST_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX,
                                      TRIAL_EXAM_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX));

    return historyManager.getSolvedProbIdSet(userId, today, "", sourceTypeList);
  }


  // USER_MASTER TB 의 사용자 정보 리턴
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
                                   "Call /userbasicinfo PUT service first. " + userId);
    }
    return userInfo;
  }


  // USER_MASTER + USER_EXAM_SCOPE TB 의 사용자 시험 정보 리턴
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
                                   "Call /userexaminfo PUT service first. " + userId);
    }
    return userExamInfo;
  }


  // (시험 대비) 시험 범위 내 소단원 Id set 설정
  public void setExamSubSectionIdSet(UserExamScope userExamScopeInfo) {
    List<String> examSubSectionIdList = userExamScopeInfo.getExceptSubSectionIdList() != null

        ? curriculumRepo.findSubSectionListBetweenExcept(userExamScopeInfo.getStartSubSectionId(),
                                                         userExamScopeInfo.getEndSubSectionId(),
                                                         Arrays.asList(userExamScopeInfo.getExceptSubSectionIdList()
                                                                                        .split(", "))) 

        : curriculumRepo.findSubSectionListBetween(userExamScopeInfo.getStartSubSectionId(),
                                                   userExamScopeInfo.getEndSubSectionId());

    // set variable
    this.examSubSectionIdSet = examSubSectionIdList.stream().collect(Collectors.toSet());
    log.info("Excepted sub section list = {}", userExamScopeInfo.getExceptSubSectionIdList());
    log.info("Total sub section list in exam = {}", this.examSubSectionIdSet);
  }


  // (시험 대비) 시험 범위 내에서, 제공할 문제가 있는 중단원 Id set 설정
  // 2021-08-19 Guik Jung
  // problemRepo.findProbCntInCurrId -> problemRepo.findExamProbCntInCurrId
  public void setValidSectionIdSet() {  
    this.validSectionIdSet = this.examSubSectionIdSet.stream()
                                                     .filter(
            subSection -> problemRepo.findExamProbCntInCurrId(subSection, this.solvedProbIdSet) != 0)
                                                     .map(subSection -> subSection.substring(0, 14))
                                                     .collect(Collectors.toSet());
  }


  // (시험 대비) 학생 시험 키워드 리턴 (학년-학기-시험종류)
  public String getUserExamKeyword(String userId) {
    // get user info
    User userInfo = getValidUserInfo(userId);

    return String.format("%s-%s-%s", userInfo.getGrade(), userInfo.getSemester(), userInfo.getExamType());
  }


  // (실력 평가) 현재 학기 내 소단원 Id 목록 리턴
  public List<String> getNormalSubSectionIdList(String userId) {
    // get user info
    User userInfo = getValidUserInfo(userId);

    // 이번 학기 마지막까지
    String endCurriculumId = 
      ExamScope.examScope.get(userInfo.getGrade() + "-" + userInfo.getSemester() + "-" + "final").get(1);

    return curriculumRepo.findSubSectionListBetween(userInfo.getCurrentCurriculumId(), endCurriculumId);
  }


  // 유형-마스터리 정보 출력
  public void printTypeMasteryList(List<TypeMasteryDTO> typeMasteryList) {	
    for (TypeMasteryDTO typeMastery : typeMasteryList)		
      log.info("\tTypeId = {}, mastery = {}", typeMastery.getTypeId(), typeMastery.getMastery());
  }


  // return schedule config
  public ScheduleConfigDTO getScheduleConfig() { 
    return ScheduleConfigDTO.builder()
                            .cardConfigList(this.cardConfigList)
                            .addtlSubSectionIdSet(this.addtlSubSectionIdSet)
                            .build();
  }


  // (실력 향상) 중단원 중간 평가 카드 여부 확인 및 생성
  public boolean checkSectionTestCard() {
    // 학년, 학기 내 소단원 목록들
    List<String> subSectionIdList = getNormalSubSectionIdList(userId);
    log.info("Total sub section list = {}", subSectionIdList);

    // 제공할 문제가 있는 중단원들
    Set<String> sectionIdSet = subSectionIdList.stream()
                                               .filter(
        subSection -> problemRepo.findProbCntInCurrId(subSection, this.solvedProbIdSet) != 0)
                                               .map(subSection -> subSection.substring(0, 14))
                                               .collect(Collectors.toSet());
    log.info("1. Total section list : {}", sectionIdSet);

    // 유형 카드를 통해 이미 푼 유형들
    List<Integer> completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "",
            TYPE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("2. Types already solved : {}", completedTypeIdList);

    // 안푼 유형들 = 앞으로 풀 유형 Id들
    this.remainTypeList = subSectionIdList.isEmpty() 
                        ? new ArrayList<>() 
                        : problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, completedTypeIdList);

    // 유형 카드가 남은 중단원들
    Set<String> notDoneSectionIdSet = completedTypeIdList.isEmpty() 
                                      ? new HashSet<>(sectionIdSet) 
                                      : remainTypeList.stream()
                                                      .map(type -> type.getCurriculumId().substring(0, 14))
                                                      .collect(Collectors.toSet());
    log.info("3. Sections not solved : {}", notDoneSectionIdSet);

    // sectionSet = 유형 카드를 완벽히 모두 푼 중단원들
    sectionIdSet.removeAll(notDoneSectionIdSet);
    log.info("4. Sections completely solved through TYPE cards : {}", sectionIdSet);

    // 중간 평가 카드를 통해 이미 푼 중단원들
    Set<String> completedSectionIdSet = historyManager.getCompletedSectionIdList(userId, today, 
            SECTION_TEST_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("5. Sections already solved through SECTION_TEST cards : {}", completedSectionIdSet);

    // sectionSet = 완벽히 푼 중단원들 - 이미 중간평가 진행한 중단원들
    sectionIdSet.removeAll(completedSectionIdSet); 
    log.info("6. Candidate sections for the SECTION_TEST card : {}", sectionIdSet);

    // sectionSet 내 중단원으로 중간 평가 카드 구성
    if (!sectionIdSet.isEmpty()) {
      String sectionId = sectionIdSet.iterator().next();
      if (problemRepo.findProbCntInCurrId(sectionId, this.solvedProbIdSet) != 0) {
        log.info("\tSECTION_TEST card. : {}", sectionId);

        this.cardConfigList.add(CardConfigDTO.builder()
                                             .cardType(SECTION_TEST_CARD_TYPESTR)
                                             .curriculumId(sectionId)
                                             .build());

        this.addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
      
        return true;
      }
    }
    return false;
  }

  
  // (실력 향상) 보충 카드 여부 확인 및 생성
  public boolean checkSuppleCard() {
    // 보충 카드를 통해 푼 유형들
    List<Integer> suppleTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", 
            SUPPLE_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("7. Type list already solved through SUPPLE cards = {}", suppleTypeIdList);

    // 가장 최근 보충 카드 이후, 유형 카드를 통해 푼 유형들
    List<Integer> solvedTypeIdList = historyManager.getCompletedTypeIdListAfterSuppleCard(userId, today);

    // 최근 유형들 - 이미 보충한 유형들 = 보충 카드로 구성될 후보 유형들
    solvedTypeIdList.removeAll(suppleTypeIdList);

    if (!solvedTypeIdList.isEmpty()) {
      // 마스터리가 낮고, 제공할 문제가 있는 유형들
      List<TypeMasteryDTO> lowTypeMasteryList = 
        userKnowledgeRepo.findNLowTypeMasteryList(userId, solvedTypeIdList, MASTERY_LOW_THRESHOLD)
            .stream()
            .filter(typeMastery -> problemRepo.findProbCntInType(typeMastery.getTypeId(), this.solvedProbIdSet) != 0)
            .collect(Collectors.toList());

      log.info("8. Low type mastery list except solved through SUPPLE cards = ");
      printTypeMasteryList(lowTypeMasteryList);

      // 보충 카드 채울 만큼 개수가 있으면, 보충 카드 구성
      if (lowTypeMasteryList.size() >= SUPPLE_CARD_TYPE_NUM) {
        log.info("\tSUPPLE card (1).");

        List<TypeMasteryDTO> typeMasteryList = lowTypeMasteryList.subList(0, SUPPLE_CARD_TYPE_NUM);

        List<Integer> suppleCardTypeIdList = 
          typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());

        this.cardConfigList.add(CardConfigDTO.builder()
                                             .cardType(SUPPLE_CARD_TYPESTR)
                                             .typeMasteryList(typeMasteryList)
                                             .build());

        this.addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
        this.totalProbNum += SUPPLE_CARD_TYPE_NUM * SUPPLE_CARD_PROB_NUM_PER_TYPE;

        // 한 장 더 채울 만큼 개수가 있으면, 보충 카드 추가 구성
        if (lowTypeMasteryList.size() >= SUPPLE_CARD_TYPE_NUM * 2) {
          log.info("\tSUPPLE card (2). ");

          typeMasteryList = lowTypeMasteryList.subList(SUPPLE_CARD_TYPE_NUM, SUPPLE_CARD_TYPE_NUM * 2);

          suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());

          this.cardConfigList.add(CardConfigDTO.builder()
                                               .cardType(SUPPLE_CARD_TYPESTR)
                                               .typeMasteryList(typeMasteryList)
                                               .build());

          this.addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
          this.totalProbNum += SUPPLE_CARD_TYPE_NUM * SUPPLE_CARD_PROB_NUM_PER_TYPE;
        }   
        return true;
      }

      // 진도를 많이 나갔는데 마스터리가 높은 경우에도, 보충 카드 구성
      else if (solvedTypeIdList.size() >= SUPPLE_CARD_TYPE_THRESHOLD) {
        log.info("\t{} types are high mastery --> SUPPLE card.", SUPPLE_CARD_TYPE_THRESHOLD);

        // 이해도가 낮은 순서 대로 유형 마스터리 가져오기
        List<TypeMasteryDTO> typeMasteryList = 
          userKnowledgeRepo.findTypeMasteryList(userId, solvedTypeIdList)
              .stream()
              .filter(typeMastery -> problemRepo.findProbCntInType(typeMastery.getTypeId(), this.solvedProbIdSet) != 0)
              .collect(Collectors.toList())
              .subList(0, SUPPLE_CARD_TYPE_NUM);

        List<Integer> suppleCardTypeIdList = typeMasteryList.stream()
                                                            .map(type -> type.getTypeId())
                                                            .collect(Collectors.toList());

        this.cardConfigList.add(CardConfigDTO.builder()
                                             .cardType(SUPPLE_CARD_TYPESTR)
                                             .typeMasteryList(typeMasteryList)
                                             .build());

        this.addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
        this.totalProbNum += SUPPLE_CARD_TYPE_NUM * SUPPLE_CARD_PROB_NUM_PER_TYPE;

        return true;
      }
    }
    return false;
  }


  // (실력 향상) 유형 카드 여부 확인 및 생성
  public boolean checkTypeCard() {
    // 문제가 없어서 카드 생성하지 못한 유형 리스트
    List<Integer> noProbTypeIdList = new ArrayList<>();

    // 앞으로 풀 유형이 있으면, 유형 카드 구성
    if (!this.remainTypeList.isEmpty()) {
      for (ProblemType type : this.remainTypeList) {

        // 제공할 문제가 없으면 pass
        if (problemRepo.findProbCntInType(type.getTypeId(), solvedProbIdSet) == 0) {
          noProbTypeIdList.add(type.getTypeId());
          continue;
        }

        log.info("TYPE card : {} ", type.getTypeId());

        this.cardConfigList.add(CardConfigDTO.builder()
                                             .cardType(TYPE_CARD_TYPESTR)
                                             .typeId(type.getTypeId())
                                             .build());

        this.addtlSubSectionIdSet.add(problemTypeRepo.findById(type.getTypeId())
                                                     .orElse(new ProblemType())
                                                     .getCurriculumId());

        this.totalProbNum += MAX_TYPE_CARD_PROB_NUM;

        // 문제 개수가 하루 최대 제공 문제 수를 넘으면 true 리턴
        if (totalProbNum >= MAX_CARD_PROB_NUM) {
          log.info("Types couldn't make cards (no problems) : {} ", noProbTypeIdList);
          return true;
        }
      }
    }
    log.info("Types couldn't make cards (no problems) : {} ", noProbTypeIdList);
    return false;
  }


  // (실력 향상) 유형 카드 여부 확인 및 생성
  public boolean checkAddtlSuppleCard() {
    // 현재 학기 내 모든 유형을 풀었으나 문제가 채워지지 않으면, 추가 보충카드 제공
    if (this.totalProbNum < MAX_CARD_PROB_NUM) {
      String examKeyword = getUserExamKeyword(userId);

      // 시험 범위 내 각 유형 마다 2문제씩 (보충 카드가 2문제)
      Integer addtiTypeNum = (int) Math.ceil((MAX_CARD_PROB_NUM - totalProbNum) / 2.0);
      log.info("ADDTL_SUPPLE card with {} problems. ", addtiTypeNum);

      // 마스터리 낮은 순서 대로, 문제가 존재하는 유형만 선택
      List<TypeMasteryDTO> addtiTypeMasteryList = 
      userKnowledgeRepo.findTypeMasteryListBetween(userId, 
                        ExamScope.examScope.get(examKeyword).get(0),
                        ExamScope.examScope.get(examKeyword).get(1))
                       .stream()
                       .filter(
        typeMastery -> problemRepo.findProbCntInType(typeMastery.getTypeId(), this.solvedProbIdSet)!=0)
                       .collect(Collectors.toList());

      if (addtiTypeMasteryList.size() < addtiTypeNum)
        return false;

      this.cardConfigList.add(CardConfigDTO.builder()
                                           .cardType(ADDTL_SUPPLE_CARD_TYPESTR)
                                           .typeMasteryList(addtiTypeMasteryList.subList(0, addtiTypeNum))
                                           .build());

      List<Integer> addtlSuppleCardTypeIdList = addtiTypeMasteryList.stream()
                                                                    .map(type -> type.getTypeId())
                                                                    .collect(Collectors.toList());

      this.addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));

      return true;
    }
    return false;
  }


  // (시험 대비) 모의고사 카드 여부 확인 및 생성
  public boolean checkTrialExamCard(Integer totalDays, Integer remainDays) {
    // 모의고사 카드 개수
    Integer numTrialExamCards = totalDays <= this.validSectionIdSet.size() + 1 ? 1 : 2;
    log.info("Num of TRIAL_EXAM cards = {}", numTrialExamCards);

    // 남은 일수가 적을 때, 모의고사 카드 제공
    if (remainDays <= numTrialExamCards) {
      String userExamKeyword = getUserExamKeyword(userId);
      log.info("\tTRIAL_EXAM card : {} ", userExamKeyword);

      this.cardConfigList.add(CardConfigDTO.builder()
                                           .cardType(TRIAL_EXAM_CARD_TYPESTR)
                                           .examKeyword(userExamKeyword)
                                           .build());

      this.addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);
      
      return true;
    }
    return false;
  }

  
  // (시험 대비) 전체 범위 시험 대비 카드 여부 확인 및 생성
  public boolean checkFullScopeExamCard(Integer totalDays, boolean isRemainCard) {    
    // 1. 중단원 개수 보다 기간이 적을 때, FULL_SCOPE_EXAM 카드 제공
    if (totalDays <= this.validSectionIdSet.size()) {
      log.info("\tTotal {} days, {} sections --> FULL_SCOPE_EXAM card.", totalDays, this.validSectionIdSet.size());
      Integer fullScopeCardProbNum = (int) Math.round((double) MAX_CARD_PROB_NUM / this.validSectionIdSet.size());

      for (String sectionId: this.validSectionIdSet) {
        log.info("FULL_SCOPE_EXAM card (Sections : {}, Probs : {}) ", sectionId, fullScopeCardProbNum);

        this.cardConfigList.add(CardConfigDTO.builder()
                                             .cardType(FULL_SCOPE_EXAM_CARD_TYPESTR)
                                             .curriculumId(sectionId)
                                             .probNum(fullScopeCardProbNum)
                                             .build());
      }

      this.addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSectionSet(this.validSectionIdSet));
  
      return true;
    }

    // 2. section exam card 다 풀고 남은 기간 동안 FULL_SCOPE_EXAM 카드 제공
    if (isRemainCard) {
      Integer fullScopeCardProbNum = (int) Math.round((double) MAX_CARD_PROB_NUM / this.validSectionIdSet.size());

      for	(String sectionId: this.validSectionIdSet) {
        log.info("FULL_SCOPE_EXAM card in remain days (Sections : {}, Probs : {}) ", sectionId, fullScopeCardProbNum);
        
        this.cardConfigList.add(CardConfigDTO.builder()
                                        .cardType(FULL_SCOPE_EXAM_CARD_TYPESTR)
                                        .curriculumId(sectionId)
                                        .probNum(fullScopeCardProbNum)
                                        .build());
      }

      this.addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSectionSet(this.validSectionIdSet));
      
      return true;
    }
    return false;
  }

  
  // (시험 대비) 중단원 시험 대비 카드 여부 확인 및 생성
  public boolean checkSectionExamCard(Integer totalDays) {    
    // SECTION_EXAM 카드 제공되는 개수
    Integer numTrialExamCards = totalDays <= this.validSectionIdSet.size() + 1 ? 1 : 2;
    Integer numSectionExamCards = Math.floorDiv(totalDays - numTrialExamCards, this.validSectionIdSet.size());

    // 각 중단원 별 카드 개수
    Map<String, Integer> sectionExamCardsNum = new HashMap<>();    
    this.validSectionIdSet.forEach(sectionId -> sectionExamCardsNum.put(sectionId, numSectionExamCards));
    log.info("2. Number of SECTION_EXAM cards per section : {} ", sectionExamCardsNum);

    // 각 중단원 별 이미 푼 카드 개수
    Map<String, Integer> completedSectionExamCardsNum = historyManager.getCompletedSectionNum(
        userId, today, SECTION_EXAM_CARD_TYPESTR + LRS_SOURCE_TYPE_POSTFIX);
    log.info("3. Number of SECTION_EXAM cards already solved : {} ", completedSectionExamCardsNum);

    // 각 중단원 별 앞으로 풀 카드 개수 계산
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

    // 카드 제공할 중단원이 있으면, 중단원 시험 대비 카드 제공
    if (!sectionExamCardsNum.keySet().isEmpty()) {
      String sectionId = curriculumRepo.sortByCurrSeq(sectionExamCardsNum.keySet()).get(0);
      log.info("SECTION_EXAM card (Section : {})", sectionId);

      this.cardConfigList.add(CardConfigDTO.builder()
                                           .cardType(SECTION_EXAM_CARD_TYPESTR)
                                           .curriculumId(sectionId)
                                           .probNum(MAX_CARD_PROB_NUM)
                                           .build());

      this.addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
      
      return true;
    }
    return false;
  }



  // 실력 향상 카드 config 리턴 : require user basic info (userId/grade/semester)
  public ScheduleConfigDTO getNormalScheduleConfig() {
    
    // check section test card available
    if (checkSectionTestCard())
      return getScheduleConfig();

                              
    // check supple card available
    checkSuppleCard();

    
    // check type card available
    if (checkTypeCard())
      return getScheduleConfig();
                        

    // check addtl supple card available
    checkAddtlSuppleCard();    
    return getScheduleConfig();
  }



  // 시험 대비 카드 config 리턴 : require user exam info (start/due date)
  public ScheduleConfigDTO getExamScheduleConfig() {

    // get user exam info
    UserExamScope userExamScopeInfo = getValidUserExamInfo(userId);
    setExamSubSectionIdSet(userExamScopeInfo);
    setValidSectionIdSet();

    if (this.validSectionIdSet.isEmpty())
      throw new RecommendException(RecommendErrorCode.NO_PROBS_ERROR, "EXAM_SCHEDULE_CONFIGS");

    // check days to prepare exam
    Integer totalDays = (int) userExamScopeInfo.getUser().getExamStartDate().toLocalDateTime()
        .until(userExamScopeInfo.getUser().getExamDueDate().toLocalDateTime(), ChronoUnit.DAYS);
        
    Integer remainDays = (int) LocalDateTime.now()
        .until(userExamScopeInfo.getUser().getExamDueDate().toLocalDateTime(), ChronoUnit.DAYS);
        
    log.info("Total exam preparation days = {}, remain days = {}", totalDays, remainDays);
    log.info("1. Sections in exam : {} ", this.validSectionIdSet);


    // check trial exam card available
    if (checkTrialExamCard(totalDays, remainDays))
      return getScheduleConfig();
    

    // check full scope exam card available
    if (checkFullScopeExamCard(totalDays, false))
      return getScheduleConfig();


    // check section exam card available
    if (checkSectionExamCard(totalDays))
      return getScheduleConfig();
    
    
    // check full scope exam card available
    checkFullScopeExamCard(totalDays, true);
    return getScheduleConfig();
  }

  

  // (실력향상) 중간평가, 보충, 유형, 추가보충, (시험대비) 모의고사 카드 1장씩 dummy로 제공, 실제 서비스 X
  public ScheduleConfigDTO getDummyScheduleConfig() {

    String examKeyword = getUserExamKeyword(userId);

    // get section id, remain type list
    String sectionId = getValidUserInfo(userId).getCurrentCurriculumId().substring(0, 14);
    List<ProblemType> typeList = problemTypeRepo.NfindRemainTypeIdList(getNormalSubSectionIdList(userId), null);


    // 유형카드 : 첫 번째 유형
    Integer typeId = typeList.get(0).getTypeId();		
    log.info("TYPE card : {}", typeId);

    this.cardConfigList.add(CardConfigDTO.builder()
                                         .cardType(TYPE_CARD_TYPESTR)
                                         .typeId(typeId)
                                         .build());

    this.addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId)
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

    this.cardConfigList.add(CardConfigDTO.builder()
                                         .cardType(SUPPLE_CARD_TYPESTR)
                                         .typeMasteryList(lowMasteryTypeList)
                                         .build());

    this.addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
    

    // 중간 평가 카드 : 현재 중단원
    log.info("SECTION_TEST card : {}", sectionId);

    this.cardConfigList.add(CardConfigDTO.builder()
                                         .cardType(SECTION_TEST_CARD_TYPESTR)
                                         .curriculumId(sectionId)
                                         .build());

    this.addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));


    // 추가 보충 카드
    Integer addtiTypeNum = (int) Math.ceil((MAX_CARD_PROB_NUM - 4) / 2.0);
    log.info("ADDTL_SUPPLE card with {} /. ", addtiTypeNum);
    
    List<TypeMasteryDTO> addtiTypeMasteryList = 
    userKnowledgeRepo.findTypeMasteryListBetween(userId, 
                      ExamScope.examScope.get(examKeyword).get(0),
                      ExamScope.examScope.get(examKeyword).get(1))
                     .stream()
                     .filter(
      typeMastery -> problemRepo.findProbCntInType(typeMastery.getTypeId(), this.solvedProbIdSet)!=0)
                     .collect(Collectors.toList())
                     .subList(0, addtiTypeNum);

    List<Integer> addtlSuppleCardTypeIdList = addtiTypeMasteryList.stream()
                                                                  .map(type -> type.getTypeId())
                                                                  .collect(Collectors.toList());
    
    this.cardConfigList.add(CardConfigDTO.builder()
                                         .cardType(ADDTL_SUPPLE_CARD_TYPESTR)
                                         .typeMasteryList(addtiTypeMasteryList)
                                         .build());

    this.addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));


    // 모의고사 카드
    log.info("TRIAL_EXAM card : {} ", examKeyword);

    this.cardConfigList.add(CardConfigDTO.builder()
                                         .cardType(TRIAL_EXAM_CARD_TYPESTR)
                                         .examKeyword(examKeyword)
                                         .build());

    this.addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);


    return getScheduleConfig();
  }
}
