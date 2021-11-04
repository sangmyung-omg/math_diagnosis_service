package com.tmax.WaplMath.Recommend.controller.diagnosis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.DiagnosisProblemsInputDTO;
import com.tmax.WaplMath.Recommend.service.problem.ProblemServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class DiagnosisController {
	@Autowired
	// @Qualifier("ProblemServiceV0")
	// @Qualifier("ProblemServiceV1")				// 중1 진입 : 안 배운 파트들은 초등 범위 문제 출제
	@Qualifier("ProblemServiceV2")				// 진단범위 선택 기능 추가 적용
	ProblemServiceBase problemService;

	/*
	*  Deprecated diagnosis api (: former version, ~ 211102)
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
		String userId = JWTUtil.getUserID(token);
		
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
	
	/*
	*  Deprecated diagnosis api (: former version, ~ 211102)
	*/
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
		String userId = JWTUtil.getUserID(token);
		
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

	/*
	*  New diagnosis api (newer version : 211102 ~)
	*/
	@GetMapping(value = "/diagnosis/scope", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getDiagnosisScope(@RequestHeader("token") String token) {				
		log.info("> DiagnosisScope logic start!");
		// token check
		if (token == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("resultMessage", "No user token");
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		// Decode JWT and get userId
		String userId = JWTUtil.getUserID(token);

		Map<String, Object> map = problemService.getDiagnosisScope(userId);
		if (map.containsKey("error"))
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		else {
			map.put("resultMessage", "Successfully returned.");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}

	/*
	*  New diagnosis api (newer version : 211102 ~)
	*/
	@PostMapping(value = "/diagnosis/problems", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getDiagnosisProblems(@RequestHeader("token") String token,
												@RequestParam("diagType") String diagType,
												@RequestBody DiagnosisProblemsInputDTO requestBody) {
		log.info("> DiagnosisProblems logic start!");
		Map<String, Object> map = problemService.getDiagnosisProblemsBySeciontIdList(requestBody.getSectionIdList(), diagType);
		
		if (map.containsKey("error"))
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		else {
			map.put("resultMessage", "Successfully returned.");
			return new ResponseEntity<>(map, HttpStatus.OK);
		}
	}
}
