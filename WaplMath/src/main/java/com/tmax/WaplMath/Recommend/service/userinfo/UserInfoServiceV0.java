package com.tmax.WaplMath.Recommend.service.userinfo;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserBasicInfoDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.model.user.UserExamScope;
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
		ResultMessageDTO output = new ResultMessageDTO();
		
		String grade = input.getGrade();
		String semester = input.getSemester();
		String name = input.getName();
		String currentCurriculumId = input.getCurrentCurriculumId();
		
		logger.info("grade:" + grade + ", semester:" + semester + ", name:" + name + ", CId:" + currentCurriculumId );
		
		// 에러 처리
		if (!grade.equalsIgnoreCase("1") && !grade.equalsIgnoreCase("2") && !grade.equalsIgnoreCase("3")) {
			output.setMessage("Value of grade should be one of '1' or '2' or '3'. Given : " + grade);
		}
		
		if (!semester.equalsIgnoreCase("1") && !semester.equalsIgnoreCase("2")) {
			if (output.getMessage() == null) {
				output.setMessage("Value of grade should be one of '1' or '2' or '3'. Given : " + grade);				
			} else {
				output.setMessage(output.getMessage() + "\nValue of semester should be one of '1' or '2'. Given : " + semester);
			}
		}
		
		if (name == null) {
			if (output.getMessage() == null) {
				output.setMessage("No value given for name");				
			} else {
				output.setMessage(output.getMessage() + "\nNo value given for name");
			}
		}
		
		if (output.getMessage() != null) {
			return output;
		}
		
		// USER_MASTER 테이블에 유저 기본 정보 저장
		User userObject = userRepository.findById(userId).orElse(new User());
		
		userObject.setUserUuid(userId);
		userObject.setGrade(grade);
		userObject.setSemester(semester);
		userObject.setName(name);
		userObject.setCurrentCurriculumId(currentCurriculumId);
		
		userRepository.save(userObject);
		
		output.setMessage("Successfully updated user basic info.");
		
		return output;
	}
}
