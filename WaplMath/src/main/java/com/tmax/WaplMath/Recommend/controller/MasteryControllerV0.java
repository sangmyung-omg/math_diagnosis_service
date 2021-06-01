package com.tmax.WaplMath.Recommend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AnalysisReport.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.service.mastery.MasteryServiceBase;

/**
 * Mastery update api controller
 * @author Sangheon Lee
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MasteryControllerV0 {

	@Autowired
	@Qualifier("MasteryServiceV0")
	private MasteryServiceBase masterySvc;

	@PutMapping(value = "/mastery", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> updateMastery(@RequestHeader("token") String token,
			@RequestBody ProblemSolveListDTO problemSolveList) throws Exception {
		//String userId = token;
        String userId = JWTUtil.getJWTPayloadField(token, "userId");
		List<String> probIdList = problemSolveList.getProbIdList();
		List<String> correctList = problemSolveList.getCorrectList();

		ResultMessageDTO resultMessage = masterySvc.updateMastery(userId, probIdList, correctList);
		return new ResponseEntity<>(resultMessage, HttpStatus.OK);

	}
}
