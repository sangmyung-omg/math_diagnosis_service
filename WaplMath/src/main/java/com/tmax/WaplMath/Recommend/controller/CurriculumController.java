package com.tmax.WaplMath.Recommend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.Recommend.service.ExamScheduleCardService;

@RestController
public class CurriculumController {

	@Autowired
	ExamScheduleCardService examScheduleCardService = new ExamScheduleCardService();
	
	
	/*
	@Autowired
	UserInfoService examInfoService = new UserInfoService();
	*/
	
	/*
	@GetMapping(value = "/ExamScheduleCard", produces = "application/json; charset=utf-8")
	public Map<String, Object> getExamScheduleCard(@RequestParam String userId, @RequestParam String date)
			throws Exception {
		System.out.println("userId: " + userId);
		System.out.println("date: " + date);
		return examScheduleCardService.getExamScheduleCard(userId, date);
	}
	*/

	/*
	@GetMapping(value = "/Mastery", produces = "application/json; charset=utf-8")
	public Map<String, Object> getMastery(@RequestParam("userId") String userId,
			@RequestParam("ukIdList") List<String> ukIdList) throws Exception {
		System.out.println("\n userId: " + userId);
		System.out.println("ukIdList: " + ukIdList);
		return masteryService.getMastery(userId, ukIdList);
	}
	
	@PutMapping(value = "/ExamInfo", produces = "application/json; charset=utf-8")
	public Map<String, String> updateExamInfo(@RequestBody Map<String, Object> input) throws Exception {
		System.out.println("inputs: " + input);
		return examInfoService.updateExamInfo(input);
	}
	*/
}
