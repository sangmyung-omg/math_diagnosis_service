package com.tmax.WaplMath.Recommend.service.userinfo;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UserRepository;

@Service
@Qualifier("UserInfoServiceV0")
public class UserInfoServiceV0 implements UserInfoServiceBase {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	private UserRepository userRepository;

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
		
		User userObject = userRepository.findById(userId).orElse(new User());
		userObject.setUserUuid(userId);
		userObject.setExamType(examType);
		userObject.setExamStartDate(Timestamp.valueOf(examStartDateTime));
		userObject.setExamDueDate(Timestamp.valueOf(examDueDateTime));
		userObject.setExamTargetScore(targetScore);
		
		userRepository.save(userObject);
		output.setMessage("Successfully update user exam info.");

		return output;
	}
}
