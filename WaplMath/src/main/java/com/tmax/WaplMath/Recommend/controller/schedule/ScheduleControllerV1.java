package com.tmax.WaplMath.Recommend.controller.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.config.RecommendConstants;
import com.tmax.WaplMath.Recommend.dto.schedule.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTO;
import com.tmax.WaplMath.Recommend.service.schedule.ScheduleServiceBase;

/**
 * Learning schedule recommendation api controller (version 1)
 * @author Sangheon Lee
 */
@RestController
@CrossOrigin(origins="*", allowedHeaders="*")
@RequestMapping(path= RecommendConstants.apiPrefix + "/v1")
public class ScheduleControllerV1 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	@Qualifier("ScheduleServiceV1")
	private ScheduleServiceBase scheduleMvc;

	@GetMapping(value = "/examschedulecard", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getExamScheduleCard(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		logger.info("userId: "+userId);
		ExamScheduleCardDTO examScheduleCard = scheduleMvc.getExamScheduleCard(userId);
		System.out.println("version 1");
		return new ResponseEntity<>(examScheduleCard, HttpStatus.OK);
	}
	
	@GetMapping(value = "/normalschedulecard", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getNormalScheduleCard(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		logger.info("userId: "+userId);
		NormalScheduleCardDTO normalScheduleCard = scheduleMvc.getNormalScheduleCard(userId);
		System.out.println("version 1");
		return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
	}
	
	@GetMapping(value = "/normalschedulecard/dummy", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getNormalScheduleCardDummy(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		logger.info("userId: "+userId);
		NormalScheduleCardDTO normalScheduleCard = scheduleMvc.getNormalScheduleCardDummy(userId);
		System.out.println("version 1");
		return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
	}
}
