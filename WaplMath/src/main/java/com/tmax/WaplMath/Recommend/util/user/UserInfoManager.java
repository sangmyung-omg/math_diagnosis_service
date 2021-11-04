package com.tmax.WaplMath.Recommend.util.user;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.model.curriculum.AcademicCalendar;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserRecommendScope;
import com.tmax.WaplMath.Common.repository.user.UserRecommendScopeRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.AcademicCalendarRepo;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserInfoManager {
  
  @Autowired
  private UserRepo userRepo;

  @Autowired
  @Qualifier("RE-AcademicCalendarRepo")
  private AcademicCalendarRepo calendarRepo;

  @Autowired
  private UserRecommendScopeRepo userScopeRepo;
  
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


  // 학생 시험 키워드 리턴 (학년-학기-시험종류)
  public String getUserExamKeyword(String userId) {
    // get user info
    User userInfo = getValidUserInfo(userId);

    return String.format("%s-%s-%s", userInfo.getGrade(), userInfo.getSemester(), userInfo.getExamType());
  }
  

  // 진입 시점을 고려해 currentCurriculumId 생성.
  public String getCurriculumIdwithCalendar(String grade, String semester, Date time){

    String currentCurriculumId;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    
    String[] dates = format.format(time).toString().split("-");
    int month = Integer.parseInt(dates[1]);
    int week = 0;
    
    /*
    // 현재 시점이 이번 달의 몇주차인지 파악.
    Calendar c = Calendar.getInstance();
    //		c.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1])-1, Integer.parseInt(dates[2]));
    c.setTime(time);
    //		c.add(Calendar.DATE, 13);		// 테스트
    //		c.add(Calendar.MONTH, -11);
    c.setFirstDayOfWeek(Calendar.MONDAY); // 월요일 기준.

    // 이번 달 마지막 주의 평일이 이틀까지면 (월, 화 로 이번 달 끝), 그 주는 다음 달의 1주차. 그보다 많으면 (수요일까지 있다) 이번 달의 마지막 주차 로 인정.
    c.setMinimalDaysInFirstWeek(5);

    week = c.get(Calendar.WEEK_OF_MONTH);

    if (week == 0) {
      c.add(Calendar.MONTH, -1);
      int y = c.get(Calendar.YEAR);
      int m = c.get(Calendar.MONTH);
      int d = c.getActualMaximum(Calendar.DAY_OF_MONTH);
      Calendar cal = Calendar.getInstance();
      cal.set(y, m, d);
      month = month - 1;
      week = cal.get(Calendar.WEEK_OF_MONTH);
    }
    */
    
    // 간단하게 1~7일 : 1주차, 8~14일 : 2주차, ......, 29~31일 : 5주차
    week = (Integer.parseInt(dates[2])-1) / 7 + 1;
  
    // 월, 주차 정보로 해당하는 커리큘럼 정보 중 가장 나중 거 curriculum_id 가져옴.
    // 1,2월은 2학기로 쳐야해서 서치에 걸리도록 month 조정
    if (month < 3) {
      month = 12 + month;
    }

    List<AcademicCalendar> curr_schedule = calendarRepo.findByMonthAndWeek(grade, semester, month, week);
    if (curr_schedule != null && curr_schedule.size() != 0) {
      currentCurriculumId = curr_schedule.get(0).getCurriculumId();			
    } else {
      currentCurriculumId = "중등-" + "중" + grade + "-" + semester + "학" + "-03-01-01";
      log.info("No curriculum info for the given month and week :" + Integer.toString(month) + "월, " + Integer.toString(week) + "주차, So setting default value to: " + currentCurriculumId);
    }
    log.info("grade={}, semester={}, month={}, week={} --> currentCurriculumId={}", grade, semester, month, week, currentCurriculumId);

    return currentCurriculumId;
  }


  // USER_RECOMMEND_SCOPE로 부터 추천 학습 소단원 id 리스트 리턴
  public List<String> getScheduleScopeSubSectionIdList(String userId){
    UserRecommendScope userScope = 
              userScopeRepo.findById(userId)
                          .orElseThrow(() -> new UserNotFoundException(userId, "not in USER_RECOMMEND_SCOPE TB."));

    return Arrays.asList(userScope.getScheduleScope().split(", "));
  }
}
