package com.tmax.WaplMath.Recommend.controller.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmax.WaplMath.AnalysisReport.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.NormalScheduleCardDTO;
import com.tmax.WaplMath.Recommend.service.schedule.ScheduleServiceBase;

@RestController
@CrossOrigin(origins="*", allowedHeaders="*")
@RequestMapping(path="/v1")
public class ScheduleControllerV1 {
	
	@Autowired
	@Qualifier("ScheduleServiceV1")
	private ScheduleServiceBase scheduleMvc;

	@GetMapping(value = "/examschedulecard", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getExamScheduleCard(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		ExamScheduleCardDTO examScheduleCard = scheduleMvc.getExamScheduleCard(userId);
		System.out.println("version 1");
		return new ResponseEntity<>(examScheduleCard, HttpStatus.OK);
	}
	
	@GetMapping(value = "/normalschedulecard", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getNormalScheduleCard(@RequestHeader("token") String token) {
		String userId = JWTUtil.getJWTPayloadField(token, "userID");
		NormalScheduleCardDTO normalScheduleCard = scheduleMvc.getNormalScheduleCard(userId);
		System.out.println("version 1");
		return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
	}
}
