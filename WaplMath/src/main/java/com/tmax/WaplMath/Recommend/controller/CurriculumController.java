package com.tmax.WaplMath.Recommend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.Recommend.service.CurriculumCardListService;
import com.tmax.WaplMath.Recommend.service.MasteryService;
import com.tmax.WaplMath.Recommend.service.UserInfoService;

@RestController
public class CurriculumController {

	/*
	@Autowired
	CurriculumCardListService curriculumCardListService = new CurriculumCardListService();

	@Autowired
	MasteryService masteryService = new MasteryService();
	
	@Autowired
	UserInfoService examInfoService = new UserInfoService();

	@GetMapping(value = "/CurriculumCardList", produces = "application/json; charset=utf-8")
	public Map<String, Object> getCurriculumCardList(@RequestParam String userId, @RequestParam String date)
			throws Exception {
		System.out.println("userId: " + userId);
		System.out.println("date: " + date);
		return curriculumCardListService.getCurriculumCardList(userId, date);
	}

	@GetMapping(value = "/Mastery", produces = "application/json; charset=utf-8")
	public Map<String, Object> getMastery(@RequestParam("userId") String userId,
			@RequestParam("ukIdList") List<String> ukIdList) throws Exception {
		System.out.println("\n userId: " + userId);
		System.out.println("ukIdList: " + ukIdList);
		return masteryService.getMastery(userId, ukIdList);
	}

	@PutMapping(value = "/Mastery", produces = "application/json; charset=utf-8")
	public Map<String, String> updateMastery(@RequestBody Map<String, Object> input) throws Exception {
		System.out.println("inputs: " + input);
		return masteryService.updateMastery(input);
	}
	
	@PutMapping(value = "/ExamInfo", produces = "application/json; charset=utf-8")
	public Map<String, String> updateExamInfo(@RequestBody Map<String, Object> input) throws Exception {
		System.out.println("inputs: " + input);
		return examInfoService.updateExamInfo(input);
	}
	*/
}
