package com.tmax.WaplMath.Recommend.service.userinfo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserBasicInfoDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;
import com.tmax.WaplMath.Recommend.event.user.UserInfoEventPublisher;
import com.tmax.WaplMath.Recommend.model.curriculum.AcademicCalendar;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.model.user.UserExamScope;
import com.tmax.WaplMath.Recommend.repository.AcademicCalendarRepo;
import com.tmax.WaplMath.Recommend.repository.UserExamScopeRepo;
import com.tmax.WaplMath.Recommend.repository.UserRepository;
import com.tmax.WaplMath.Recommend.util.ExamScope;

@Service
@Qualifier("UserInfoServiceV0")
public class UserInfoServiceV0 implements UserInfoServiceBase {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserExamScopeRepo userExamScopeRepo;

	@Autowired
	private AcademicCalendarRepo calendarRepo;
	
	@Autowired
	UserInfoEventPublisher userInfoEventPublisher;
	

	Timestamp getTimestamp(String timeStr){
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
		List<String> input = new ArrayList<String>();
		input.add(userId);
		logger.info("Getting user basic info...");
		List<User> queryList = (List<User>) userRepo.findAllById(input);
		logger.info("user : " + input + ", Query Result Size: " + Integer.toString(queryList.size()));
		if (queryList.size() != 0 && queryList != null) {
			result = queryList.get(0);
		}
		return result;
	}

	@Override
	public ResultMessageDTO updateExamInfo(String userId, UserExamInfoDTO input) {
		ResultMessageDTO output = new ResultMessageDTO();

		// load user_master tb
		User user = userRepo.findById(userId).orElse(new User());
		user.setUserUuid(userId);
		
		// parse input
		String examStartDate = input.getExamStartDate();
		String examDueDate = input.getExamDueDate();
		
		// parse date format
		try{
			user.setExamStartDate(getTimestamp(examStartDate));
			user.setExamDueDate(getTimestamp(examDueDate));
		} catch(Exception e){
			output.setMessage("'examStartDate' or 'examDueDate' should be in shape of 'yyyy-MM-dd'.");
			return output;
		}

		// update user_master tb
		userRepo.save(user);

		// load user_exam_scope tb
		UserExamScope userExamScope = userExamScopeRepo.findById(userId).orElse(new UserExamScope());
		userExamScope.setUserUuid(userId);		
		Boolean isExamScopeChanged = false;
		
		// parse input
		String startSubSectionId = input.getStartSubSectionId();
		String endSubSectionId = input.getEndSubSectionId();
		List<String> exceptSubSectionIdList = input.getExceptSubSectionIdList();
		
		if (startSubSectionId != null && endSubSectionId != null) {
			logger.info("startSubSectionId:" + startSubSectionId + ", endSubSectionId:" + endSubSectionId);
			if (!(userExamScope.getStartSubSectionId().equals(startSubSectionId) && 
				  userExamScope.getEndSubSectionId().equals(endSubSectionId)))
				isExamScopeChanged = true;
			userExamScope.setStartSubSectionId(startSubSectionId);
			userExamScope.setEndSubSectionId(endSubSectionId);
		}
		// excepted sub section is not null
		if (exceptSubSectionIdList != null) {
			logger.info("exceptSubSectionIdList:" + exceptSubSectionIdList);
			String exceptSubSectionIdStr = exceptSubSectionIdList.toString().replace("[", "").replace("]", "");
			if (userExamScope.getExceptSubSectionIdList() == null && !exceptSubSectionIdStr.equals(""))
				isExamScopeChanged = true;
			else if (userExamScope.getExceptSubSectionIdList() != null && !userExamScope.getExceptSubSectionIdList().equals(exceptSubSectionIdStr))
				isExamScopeChanged = true;
			userExamScope.setExceptSubSectionIdList(exceptSubSectionIdStr);
		}
		// update user_exam_scope tb
		userExamScopeRepo.save(userExamScope);
		
		// Publish exam scope change event only if exam scope changed
		if (isExamScopeChanged)	userInfoEventPublisher.publishExamScopeChangeEvent(userId);
		
		// if able to set exam type
//		String examType = input.getExamType();
//		if (!examType.equals("mid") && !examType.equals("final")) {
//			output.setMessage("'examType' should be either 'mid' or 'final'.");
//			return output;
//		}
//		userObject.setExamType(examType);
//
//		String grade = userObject.getGrade();
//		String semester = userObject.getSemester();
//
//		if (grade != null && semester != null) {
//			String examRangeKey = grade + "-" + semester + "-" + examType;
//			List<String> examRangeSubSection = examScope.get(examRangeKey);
//
//			String startSubSection = examRangeSubSection.get(0);
//			String endSubSection = examRangeSubSection.get(1);
//
//			UserExamScope userExamScope = userExamScopeRepo.findById(userId).orElse(new UserExamScope());
//
//			userExamScope.setUserUuid(userId);
//			userExamScope.setStartSubSectionId(startSubSection);
//			userExamScope.setEndSubSectionId(endSubSection);
//
//			userExamScopeRepo.save(userExamScope);
//		}

		output.setMessage("Successfully update user exam info.");
		return output;
	}

	@Override
	public ResultMessageDTO updateBasicInfo(String userId, UserBasicInfoDTO input) {
		// 중간-기말 , 기말-다음학기 구분하는 임시 날짜
		final String SPRING_MID_TERM = "2021-05-00";
		final String SPRING_END = "2021-08-15";
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
		
		logger.info("userId:" + userId + ", grade:" + grade + ", semester:" + semester + ", name:" + name + ", CId:" + currentCurriculumId+ ", targetScore:" + targetScore);

		if (name == null) {
			if (output.getMessage() == null) {
				output.setMessage("No value given for name");
			} else {
				output.setMessage(output.getMessage() + "\nNo value given for name");
			}
		}
		
		// 내부 테스트 시에만 currentCurriculumId 값 받기로 함. 외부 콜 시 값 없음.
		if (currentCurriculumId == null) {
			// 진입 시점을 고려해 currentCurriculumId 생성.
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
				logger.info("No curriculum info for the given month and week :" + Integer.toString(month) + "월, " + Integer.toString(week) + "주차, So setting default value to: " + currentCurriculumId);
				logger.info(grade + ", " + semester + ", " + Integer.toString(month) + ", " + Integer.toString(week));
			}
		}
		
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

		// (시험 범위, 단원) 맵핑 정보를 통해 start_sub_section과 end_sub_section 정보 얻기
		List<String> scope = ExamScope.examScope.get(grade+"-"+semester+"-"+term);
		String start_sub_section = scope.get(0);
		String end_sub_section = scope.get(1);
		UserExamScope userExamScope = new UserExamScope();
		
		// USER_EXAM_SCOPE 테이블에 시험 범위 시작 단원, 끝 단원 정보 입력
		userExamScope.setUserUuid(userId);
		userExamScope.setStartSubSectionId(start_sub_section);
		userExamScope.setEndSubSectionId(end_sub_section);
		
		userExamScopeRepo.save(userExamScope);
		
		// Publish school info change event only if user info changed
		if (isUserInfoChanged) userInfoEventPublisher.publishSchoolInfoChangeEvent(userId);
		
		output.setMessage("Successfully updated user basic info.");

		return output;
	}

	@Override
	public ResultMessageDTO deleteUserInfo(String userId) {
		userRepo.deleteById(userId);
		return new ResultMessageDTO("Successfully delete user info");
	}
}
