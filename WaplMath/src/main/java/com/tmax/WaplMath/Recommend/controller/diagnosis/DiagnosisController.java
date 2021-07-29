package com.tmax.WaplMath.Recommend.controller.diagnosis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.service.problem.ProblemServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DiagnosisController {
	@Autowired
	@Qualifier("ProblemServiceV0")
	ProblemServiceBase problemService;
	/*
	@Autowired
	@Qualifier("UserInfoServiceV0")
	UserInfoService userService;
	
	
	@Autowired
	MasteryService	masteryService;
	
	@Autowired
	CurriculumService curriculumService;
	
	*/
	@GetMapping(value = "/diagnosis", produces = "application/json; charset=utf-8")
//	@GetMapping(value = "/DiagnosisProblems", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getDiagnosisProblems(
			@RequestHeader("token") String token,
			@RequestParam String diagType) throws Exception {
		// token check
		if (token == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("resultMessage", "No user token");
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		// Decode JWT and get userId
		String userId = JWTUtil.getUserID(token, false);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map = problemService.getDiagnosisProblems(userId, diagType);
		if (map == null || map.keySet().size() == 0) {
			map.put("resultMessage", "error : result of getnextProblemSet is empty, " + map);
		} else {
			if (map.containsKey("error")) {
				map.put("resultMessage", map.get("error"));
				map.remove("error");
				return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
			} 
			map.put("resultMessage", "Successfully returned");		
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	@GetMapping(value = "/diagnosis/extra", produces = "application/json; charset=utf-8")
//	@GetMapping(value = "/ExtraProblems", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getAdaptiveProblem(
			@RequestHeader("token") String token,
			@RequestParam List<Integer> probIdList) throws Exception {
		// token check
		if (token == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("resultMessage", "No user token");
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// Decode JWT and get userId
		String userId = JWTUtil.getUserID(token, false);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map = problemService.getExtraProblem(userId, probIdList);
		if (map == null || map.keySet().size() == 0) {
			map.put("resultMessage", "error : result of getnextProblemSet is null, " + map);
		} else {
			if (map.containsKey("error")) {
				map.put("resultMessage", map.get("error"));
				map.remove("error");
				return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
			} 
			map.put("resultMessage", "Successfully returned");		
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
}
