package com.tmax.WaplMath.Recommend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.Recommend.model.UserCurrentInfoInput;
import com.tmax.WaplMath.Recommend.service.CurriculumService;
import com.tmax.WaplMath.Recommend.service.MasteryService;
import com.tmax.WaplMath.Recommend.service.ProblemService;
import com.tmax.WaplMath.Recommend.service.UserInfoService;


@RestController
public class DiagnosisController {
	@Autowired
	ProblemService problemService;
	/*
	@Autowired
	UserInfoService userService;
	
	
	@Autowired
	MasteryService	masteryService;
	
	@Autowired
	CurriculumService curriculumService;
	
	*/
	@GetMapping(value = "/NextProblemSet", produces = "application/json; charset=utf-8")
	public Map<String, Object> getNextProblemSet(
			@RequestParam String token,
			@RequestParam String diagType) throws Exception {
		if (token == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("resultMessage", "No user token");
			return map;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map = problemService.getNextProblemSet(token, diagType);
		if (map == null || map.keySet().size() == 0) {
			map.put("resultMessage", "error : result of getnextProblemSet is null, " + map);
		} else {
			if (map.containsKey("error")) {
				map.put("resultMessage", map.get("error"));
				map.remove("error");
				return map;
			} 
			map.put("resultMessage", "Successfully returned");		
		}
		return map;
	}
	
	@GetMapping(value = "/AdaptiveProblem", produces = "application/json; charset=utf-8")
	public Map<String, Object> getAdaptiveProblem(
			@RequestParam String token,
			@RequestParam String diagType,
			@RequestParam Integer probId) throws Exception {
		if (token == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("resultMessage", "No user token");
			return map;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map = problemService.getAdaptiveProblem(token, diagType, probId);
		if (map == null || map.keySet().size() == 0) {
			map.put("resultMessage", "error : result of getnextProblemSet is null, " + map);
		} else {
			if (map.containsKey("error")) {
				map.put("resultMessage", map.get("error"));
				map.remove("error");
				return map;
			} 
			map.put("resultMessage", "Successfully returned");		
		}
		return map;
	}
}
