package com.tmax.WaplMath.Recommend.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.model.Curriculum;
import com.tmax.WaplMath.Recommend.model.User;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.UserRepository;

@Service
public class UserInfoService {
	/*
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private CurriculumRepository curriculumRepository;

	@Autowired
	private UserRepository userRepository;

	public List<String> getChapterNameList(String grade, String semester) {
		List<String> list = new ArrayList<String>();
		String queryString = "중등-중" + grade + "-" + semester + "학-%";
		logger.info("Getting chapter name list...");
		List<String> queryList = curriculumRepository.findAllByCurriculumIdLike(queryString);
		for (String str : queryList) {
			list.add(str);
		}

		return list;
	}

	public String updateUserCurrentInfo(String userId, String grade, String semester, String chapter) {
		User user = new User();
		user.setUserUuid(userId);
		user.setGrade(grade);
		user.setSemester(semester);
		System.out.println("updateUserCurrentInfo CHAPTER : " + chapter);
		logger.info("Getting chapter id...");
		try {
			Curriculum dao = curriculumRepository.findByChapter(grade, chapter);
			System.out.println("DAO of CURRICULUM_MASTER : " + dao + chapter);
			user.setCurrentCurriculumId(dao.getChapterId());
		} catch (Exception e) {
			e.printStackTrace();
			return "error : SELECTing chapterId by chapter's name SQL went wrong";
		}
		if (user.getCurrentCurriculumId() != null) {
			logger.info("Updating user info...");
			userRepository.save(user);
		} else
			return "error : Check the chatper! No chapterId matched with the chapter name";
		return "Successfully updated";
	}

	public User getUserInfo(String userId) {
		User result = new User();
		List<String> input = new ArrayList<String>();
		input.add(userId);
		logger.info("Getting user basic info...");
		List<User> queryList = (List<User>) userRepository.findAllById(input);
		System.out.println("####################" + input + ", " + queryList);
		if (queryList.size() != 0 && queryList != null) {
			result = queryList.get(0);			
		}
		return result;
	}

	public Map<String, String> updateExamInfo(Map<String, Object> input) {
		Map<String, String> output = new HashMap<String, String>();
		String userId = (String) input.get("userId");
		String examType = (String) input.get("examType");
		String examDate = (String) input.get("examDate");
		String targetScore = (String) input.get("targetScore");

		if (!examType.equals("mid") && !examType.equals("final")) {
			output.put("resultMessage", "'examType' should be either 'mid' or 'final'.");
			return output;
		}

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime examDateTime;
		try {
			examDateTime = LocalDate.parse(examDate, dtf).atStartOfDay();
		} catch (DateTimeParseException e) {
			output.put("resultMessage", "'examDate' should be in shape of 'yyyy-MM-dd'.");
			return output;
		}
		
		Integer targetScoreInt;
		try {
			targetScoreInt = Integer.parseInt(targetScore);
		} catch (NumberFormatException e) {
			output.put("resultMessage", "'targetScore' should be a string of integer.");
			return output;
		}
				
		User userDAO = userRepository.findById(userId).orElse(new User());
		userDAO.setUserUuid(userId);
		userDAO.setExamType(examType);
		userDAO.setExamStartDate(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
		userDAO.setExamDueDate(Timestamp.valueOf(examDateTime));
		userDAO.setExamTargetScore(targetScoreInt);

		userRepository.save(userDAO);
		output.put("resultMessage", "Successfully update user exam info.");

		return output;
	}
	*/
}
