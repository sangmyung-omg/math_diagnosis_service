package com.tmax.WaplMath.Recommend.service.userinfo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserBasicInfoDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;
import com.tmax.WaplMath.Recommend.model.curriculum.AcademicCalendar;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.model.user.UserExamScope;
import com.tmax.WaplMath.Recommend.repository.AcademicCalendarRepo;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.UserExamScopeRepo;
import com.tmax.WaplMath.Recommend.repository.UserRepository;
import com.tmax.WaplMath.Recommend.util.ExamScope;

@Service
@Qualifier("UserInfoServiceV0")
public class UserInfoServiceV0 implements UserInfoServiceBase {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserExamScopeRepo userExamScopeRepo;

	@Autowired
	private CurriculumRepository curriculumRepo;
	
	@Autowired
	private AcademicCalendarRepo calendarRepo;

	@Autowired
	private static Map<String, List<String>> examScope = ExamScope.examScope;

	@Override
	public User getUserInfo(String userId) {
		User result = new User();
		List<String> input = new ArrayList<String>();
		input.add(userId);
		logger.info("Getting user basic info...");
		List<User> queryList = (List<User>) userRepository.findAllById(input);
		logger.info("user : " + input + ", Query Result Size: " + Integer.toString(queryList.size()));
		if (queryList.size() != 0 && queryList != null) {
			result = queryList.get(0);
		}
		return result;
	}

	@Override
	public ResultMessageDTO updateExamInfo(String userId, UserExamInfoDTO input) {
		ResultMessageDTO output = new ResultMessageDTO();

		String examType = input.getExamType();
		String examStartDate = input.getExamStartDate();
		String examDueDate = input.getExamDueDate();
		Integer targetScore = input.getTargetScore();

		if (!examType.equals("mid") && !examType.equals("final")) {
			output.setMessage("'examType' should be either 'mid' or 'final'.");
			return output;
		}

		LocalDateTime examStartDateTime, examDueDateTime;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		try {
			examStartDateTime = LocalDate.parse(examStartDate, dtf).atStartOfDay();
			examDueDateTime = LocalDate.parse(examDueDate, dtf).atStartOfDay();
		} catch (DateTimeParseException e) {
			output.setMessage("'examStartDate' or 'examDueDate' should be in shape of 'yyyy-MM-dd'.");
			return output;
		}

		// update user_master tb
		User userObject = userRepository.findById(userId).orElse(new User());

		userObject.setUserUuid(userId);
		userObject.setExamType(examType);
		userObject.setExamStartDate(Timestamp.valueOf(examStartDateTime));
		userObject.setExamDueDate(Timestamp.valueOf(examDueDateTime));
		userObject.setExamTargetScore(targetScore);

		userRepository.save(userObject);

		// update user_exam_scope tb
		String grade = userObject.getGrade();
		String semester = userObject.getSemester();

		if (grade != null && semester != null) {
			String examRangeKey = grade + "-" + semester + "-" + examType;
			List<String> examRangeSubSection = examScope.get(examRangeKey);

			String startSubSection = examRangeSubSection.get(0);
			String endSubSection = examRangeSubSection.get(1);

			UserExamScope userExamScope = userExamScopeRepo.findById(userId).orElse(new UserExamScope());

			userExamScope.setUserUuid(userId);
			userExamScope.setStartSubSection(startSubSection);
			userExamScope.setEndSubSection(endSubSection);

			userExamScopeRepo.save(userExamScope);
		}

		output.setMessage("Successfully update user exam info.");

		return output;
	}

	@Override
	public ResultMessageDTO updateBasicInfo(String userId, UserBasicInfoDTO input) {
		// 중간-기말 , 기말-다음학기 구분하는 임시 날짜
		final String SPRING_MID_TERM = "2021-05-10";
		final String SPRING_VACATION = "2021-07-19";
		final String FALL_MID_TERM = "2021-10-11";
		final String FALL_VACATION = "2022-01-01";
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date time = new Date();
				
		
		ResultMessageDTO output = new ResultMessageDTO();

		String grade = input.getGrade();
		String semester = "";
		if (format.format(time).toString().compareToIgnoreCase(SPRING_VACATION) < 0) {
			semester = "1";
		} else semester = "2";
		String name = input.getName();
		String currentCurriculumId = input.getCurrentCurriculumId();

		logger.info("userId:" + userId + ", grade:" + grade + ", semester:" + semester + ", name:" + name + ", CId:" + currentCurriculumId);

		// 에러 처리
		if (!grade.equalsIgnoreCase("1") && !grade.equalsIgnoreCase("2") && !grade.equalsIgnoreCase("3")) {
			output.setMessage("Value of grade should be one of '1' or '2' or '3'. Given : " + grade);
		}

		if (!semester.equalsIgnoreCase("1") && !semester.equalsIgnoreCase("2")) {
			if (output.getMessage() == null) {
				output.setMessage("Value of grade should be one of '1' or '2' or '3'. Given : " + grade);
			} else {
				output.setMessage(
						output.getMessage() + "\nValue of semester should be one of '1' or '2'. Given : " + semester);
			}
		}

		if (name == null) {
			if (output.getMessage() == null) {
				output.setMessage("No value given for name");
			} else {
				output.setMessage(output.getMessage() + "\nNo value given for name");
			}
		}
		
		// 진입 시점을 고려해 currentCurriculumId 생성.
		String[] dates = format.format(time).toString().split("-");
		int month = Integer.parseInt(dates[1]);
		int week = 0;
		
		// 현재 시점이 이번 달의 몇주차인지 파악.
		Calendar c = Calendar.getInstance();
//		c.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1])-1, Integer.parseInt(dates[2]));
		c.setTime(time);
//		c.add(Calendar.DATE, 13);		// 테스트
//		c.add(Calendar.MONTH, -11);
		c.setFirstDayOfWeek(Calendar.MONDAY);		// 월요일 기준.
		
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
		
		// 월, 주차 정보로 해당하는 커리큘럼 정보 중 가장 나중 거 curriculum_id 가져옴.
		List<AcademicCalendar> curr_schedule = calendarRepo.findByMonthAndWeek(grade, month, week);
		if (curr_schedule != null | curr_schedule.size() != 0) {
			currentCurriculumId = curr_schedule.get(0).getCurriculumId();			
		}
		
		
		if (currentCurriculumId == null) {
			currentCurriculumId = "중등-" + "중" + grade + "-" + semester + "학" + "-03";
			logger.info("currentCurriculumId is null. Set default value to: " + currentCurriculumId);
		}

		try {
			Curriculum curriculum = curriculumRepo.findById(currentCurriculumId).orElseThrow(() -> new Exception());
		} catch (Exception e) {
			if (output.getMessage() == null) {
				output.setMessage("Current curriculum id is not valid. Given : " + currentCurriculumId);
			} else {
				output.setMessage(
						output.getMessage() + "Current curriculum id is not valid. Given : " + currentCurriculumId);
			}
		}

		if (output.getMessage() != null) {
			return output;
		}
		
//		logger.info(userId);
		// USER_MASTER 테이블에 유저 기본 정보 저장
		User userObject = userRepository.findById(userId).orElse(new User());
//		logger.info(userObject.toString());
		userObject.setUserUuid(userId);
		userObject.setGrade(grade);
		userObject.setSemester(semester);
		userObject.setName(name);
		userObject.setCurrentCurriculumId(currentCurriculumId);

		userRepository.save(userObject);

//		// Foreign key constraint violation TEST
//		Optional<User> userObject = userRepository.findById(userId);
//		if (userObject.isPresent()) {
//			logger.info("exists");
//			userObject.get().setUserUuid(userId);
//			userObject.get().setGrade(grade);
//			userObject.get().setSemester(semester);
//			userObject.get().setName(name);
//			userObject.get().setCurrentCurriculumId(currentCurriculumId);
//			userRepository.save(userObject.get());
//		} else {
//			logger.info(userObject.toString());
//			output.setMessage("queryResult is empty:" + userObject.toString());
//			return output;
//		}
		
		
		output.setMessage("Successfully updated user basic info.");
		
		return output;
	}
}
