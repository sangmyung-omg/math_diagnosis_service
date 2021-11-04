package com.tmax.WaplMath.Recommend.service.userinfo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserExamScope;
import com.tmax.WaplMath.Common.model.user.UserRecommendScope;
import com.tmax.WaplMath.Common.repository.user.UserExamScopeRepo;
import com.tmax.WaplMath.Common.repository.user.UserRecommendScopeRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserBasicInfoDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;
import com.tmax.WaplMath.Recommend.event.user.UserInfoEventPublisher;
import com.tmax.WaplMath.Recommend.repository.AcademicCalendarRepo;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.user.UserInfoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("UserInfoServiceV0")
@Slf4j
public class UserInfoServiceV0 implements UserInfoServiceBase {

  @Autowired
  private UserRepo userRepo;

  @Autowired
  private UserExamScopeRepo userExamScopeRepo;

  @Autowired
  @Qualifier("RE-AcademicCalendarRepo")
  private AcademicCalendarRepo calendarRepo;

  @Autowired
  private UserRecommendScopeRepo userScopeRepo;
  
  @Autowired
  @Qualifier("RE-CurriculumRepo")
  private CurriculumRepo curriculumRepo;

  @Autowired
  UserInfoEventPublisher userInfoEventPublisher;
  
  @Autowired
  private UserInfoManager userInfoManager;

  @Autowired
  @Qualifier("UserScheduleScopeServiceV0")
  private UserScheduleScopeServiceV0 userScheduleService;


  public Timestamp getTimestamp(String timeStr){
    if (timeStr == null)	return null;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime dateTime;

    try {
      dateTime = LocalDate.parse(timeStr, dtf).atStartOfDay();
    } catch (DateTimeParseException e) {
      throw e;
    }

    return Timestamp.valueOf(dateTime);
  }


  @Override
  public User getUserInfo(String userId) {

    User result = new User();
    log.info("Getting user basic info...");

    List<User> queryList = (List<User>) userRepo.findAllById(Arrays.asList(userId));
    log.info("user : " + userId + ", Query Result Size: " + Integer.toString(queryList.size()));

    if (queryList.size() != 0 && queryList != null)
      result = queryList.get(0);

    return result;
  }


  @Override
  public ResultMessageDTO updateExamInfo(String userId, UserExamInfoDTO input) {

    // load user_master tb
    User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId, "not in USER_MASTER TB."));
    user.setUserUuid(userId);
    
    log.info("user exam info input: {}", input);

    // parse input
    String examStartDate = input.getExamStartDate();
    String examDueDate = input.getExamDueDate();
    
    // parse date format
    if (examStartDate != null && examDueDate != null) {
      try {
        user.setExamStartDate(getTimestamp(examStartDate));
        user.setExamDueDate(getTimestamp(examDueDate));
      } catch (Exception e) {
        return new ResultMessageDTO("'examStartDate' or 'examDueDate' should be in shape of 'yyyy-MM-dd'.");
      }
    }

    // update user_master tb
    userRepo.save(user);

    // load user_exam_scope tb
    UserExamScope userExamScope =
        userExamScopeRepo.findById(userId)
                         .orElseThrow(() -> new UserNotFoundException(userId, "not in USER_EXAM_SCOPE TB."));

    userExamScope.setUserUuid(userId);
    boolean isExamScopeChanged = false;
    
    // parse input
    String startSubSectionId = input.getStartSubSectionId();
    String endSubSectionId = input.getEndSubSectionId();
    List<String> exceptSubSectionIdList = input.getExceptSubSectionIdList();
    
    if (startSubSectionId != null && endSubSectionId != null) {
      log.info("startSubSectionId: {}, endSubSectionId: {}", startSubSectionId, endSubSectionId);

      if (!(userExamScope.getStartSubSectionId().equals(startSubSectionId) && 
            userExamScope.getEndSubSectionId().equals(endSubSectionId)))
        isExamScopeChanged = true;

      userExamScope.setStartSubSectionId(startSubSectionId);
      userExamScope.setEndSubSectionId(endSubSectionId);
    }

    // excepted sub section is not null
    if (exceptSubSectionIdList != null) {
      log.info("exceptSubSectionIdList: {}", exceptSubSectionIdList);

      String exceptSubSectionIdStr = exceptSubSectionIdList.toString()
                                                           .replace("[", "").replace("]", "");

      if (userExamScope.getExceptSubSectionIdList() == null && !exceptSubSectionIdStr.equals(""))
        isExamScopeChanged = true;

      else if (userExamScope.getExceptSubSectionIdList() != null
               && !userExamScope.getExceptSubSectionIdList().equals(exceptSubSectionIdStr))
        isExamScopeChanged = true;

      userExamScope.setExceptSubSectionIdList(exceptSubSectionIdStr);
    }
    // update user_exam_scope tb
    userExamScopeRepo.save(userExamScope);
    
    // Publish exam scope change event only if exam scope changed
    if (isExamScopeChanged)	userInfoEventPublisher.publishExamScopeChangeEvent(userId);

    return new ResultMessageDTO("Successfully update user exam info.");
  }


  @Override
  public ResultMessageDTO updateBasicInfo(String userId, UserBasicInfoDTO input) {
    // 중간-기말 , 기말-다음학기 구분하는 임시 날짜
    final String SPRING_MID_TERM = "2021-05-00";
    final String SPRING_END = "2021-08-00";
    final String FALL_MID_TERM = "2021-10-00";
    final String FALL_END = "2022-03-00";

    Boolean isUserInfoChanged = false;
    
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Date time = new Date();

    ResultMessageDTO output = new ResultMessageDTO();

    String grade = input.getGrade();

    String name = input.getName();
    String currentCurriculumId = input.getCurrentCurriculumId();		
    Integer targetScore = input.getTargetScore();
    // 학기 결정
    String semester = format.format(time).toString().compareToIgnoreCase(SPRING_END) < 0 ? "1" : "2";
    // 시험 범위 (mid / final) 판단해서 USER_EXAM_SCOPE 테이블에 시작 소단원, 끝 소단원 넣기
    String term = "";
    if (semester.equalsIgnoreCase("1")) {
      if (format.format(time).toString().compareToIgnoreCase(SPRING_MID_TERM) < 0) {
        term = "mid";
      } else term = "final";
    } else if (semester.equalsIgnoreCase("2")){
      if (format.format(time).toString().compareToIgnoreCase(FALL_MID_TERM) < 0) {
        term = "mid";
      } else term = "final";
    }
    
    log.info("userId:" + userId + ", grade:" + grade + ", semester:" + semester + ", name:" + name + ", CId:" + currentCurriculumId+ ", targetScore:" + targetScore);

    if (name == null) {
      if (output.getMessage() == null) {
        output.setMessage("No value given for name");
      } else {
        output.setMessage(output.getMessage() + "\nNo value given for name");
      }
    }
    
    // 내부 테스트 시에만 currentCurriculumId 값 받기로 함. 외부 콜 시 값 없음.
    if (currentCurriculumId == null)
      currentCurriculumId = userInfoManager.getCurriculumIdwithCalendar(grade, semester, time);
    
    // USER_MASTER 테이블에 유저 기본 정보 저장
    User userObject = userRepo.findById(userId).orElse(new User());

    // Check if user info changed
    if (userObject.getGrade() == null && grade != null)
      isUserInfoChanged = true;
    if (grade != null && userObject.getGrade() != null && !(userObject.getGrade().equals(grade) && userObject.getSemester().equals(semester) && term.equals(userObject.getExamType())))
      isUserInfoChanged = true;
    
    userObject.setUserUuid(userId);
    if (grade != null)	userObject.setGrade(grade);
    if (grade != null)	userObject.setSemester(semester);
    if (name != null)	userObject.setName(name);
    if (currentCurriculumId != null)	userObject.setCurrentCurriculumId(currentCurriculumId);
    if (currentCurriculumId != null)	userObject.setExamType(term);
    if (targetScore != null)	userObject.setExamTargetScore(targetScore);
    
    userRepo.save(userObject);
    log.info("Successfully save to USER_MASTER table.");

    // (시험 범위, 단원) 맵핑 정보를 통해 start_sub_section과 end_sub_section 정보 얻기
    List<String> scope = ExamScope.examScope.get(grade+"-"+semester+"-"+term);
    String startSubSectionId = scope.get(0);
    String endSubSectionId = scope.get(1);
    
    // USER_EXAM_SCOPE 테이블에 시험 범위 시작 단원, 끝 단원 정보 입력
    UserExamScope userExamScope = new UserExamScope();
    userExamScope.setUserUuid(userId);
    userExamScope.setStartSubSectionId(startSubSectionId);
    userExamScope.setEndSubSectionId(endSubSectionId);

    userExamScopeRepo.save(userExamScope);
    log.info("Successfully save to USER_EXAM_SCOPE table.");
    
    // USER_RECOMMEND_SCOPE 테이블에 추천 범위 정보 입력
    UserRecommendScope userScope = userScopeRepo.findById(userId).orElse(new UserRecommendScope());
    userScope.setUserUuid(userId);
    userScope.setScheduleScope(curriculumRepo.findSubSectionListBetween(currentCurriculumId, endSubSectionId)
                                             .toString().replace("[", "").replace("]", ""));
    
    userScopeRepo.save(userScope);
    log.info("Successfully save to USER_RECOMMEND_SCOPE table.");

    // Publish school info change event only if user info changed
    if (isUserInfoChanged) userInfoEventPublisher.publishSchoolInfoChangeEvent(userId);
    
    output.setMessage("Successfully updated user basic info.");

    return output;
  }


  @Override
  public ResultMessageDTO deleteUserInfo(String userId) {
    if(userRepo.existsById(userId)) {
      userRepo.deleteById(userId);

      //Publish delete event to listeners
      userInfoEventPublisher.publishUserDelete(userId);

      return new ResultMessageDTO("Successfully delete user info");
    } else {
      return new ResultMessageDTO(String.format("Warning: User %s is not in USER_MASTER TB", userId));
    }
  }
}
