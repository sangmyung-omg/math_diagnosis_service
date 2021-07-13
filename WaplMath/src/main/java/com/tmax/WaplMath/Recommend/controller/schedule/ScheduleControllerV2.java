package com.tmax.WaplMath.Recommend.controller.schedule;

import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.config.RecommendConstants;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTOV2;
import com.tmax.WaplMath.Recommend.service.schedule.ScheduleServiceBaseV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = RecommendConstants.apiPrefix + "/v2")
public class ScheduleControllerV2 {

	@Autowired
	@Qualifier("ScheduleServiceV2")
	private ScheduleServiceBaseV2 scheduleMvc;

	@GetMapping(value = "/normalschedulecard", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getNormalScheduleCard(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		log.info("userId: " + userId);
		NormalScheduleCardDTOV2 normalScheduleCard = scheduleMvc.getNormalScheduleCard(userId);
		log.info("version 2");
		return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
	}

	@GetMapping(value = "/examschedulecard", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getExamScheduleCard(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		log.info("userId: " + userId);
		NormalScheduleCardDTOV2 examScheduleCard = scheduleMvc.getExamScheduleCard(userId);
		log.info("version 2");
		return new ResponseEntity<>(examScheduleCard, HttpStatus.OK);
	}

	@GetMapping(value = "/normalschedulecard/dummy", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getNormalScheduleCardDummy(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		log.info("userId: " + userId);
		NormalScheduleCardDTOV2 normalScheduleCard = scheduleMvc.getNormalScheduleCardDummy(userId);
		System.out.println("version 2");
		return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
	}
}
