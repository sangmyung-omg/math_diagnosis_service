package com.tmax.WaplMath.Recommend.controller.schedule;

import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.config.RecommendConstants;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleCardOutputDTO;
import com.tmax.WaplMath.Recommend.service.schedule.v2.ScheduleServiceBaseV2;
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
  public ResponseEntity<Object> getNormalScheduleCard(@RequestHeader("token") String token) {

    String userId = JWTUtil.getJWTPayloadField(token, "userID");

    log.info("Normal card service. userId : {}", userId);

    ScheduleCardOutputDTO normalScheduleCard = scheduleMvc.getNormalScheduleCard(userId);

    log.info("Successfully return normal cards. ");

    return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
  }


  @GetMapping(value = "/examschedulecard", produces = "application/json; charset=utf-8")
  public ResponseEntity<Object> getExamScheduleCard(@RequestHeader("token") String token) {

    String userId = JWTUtil.getJWTPayloadField(token, "userID");
    
    log.info("Exam card service. userId : {}", userId);

    ScheduleCardOutputDTO examScheduleCard = scheduleMvc.getExamScheduleCard(userId);
    
    log.info("Successfully return exam cards. ");

    return new ResponseEntity<>(examScheduleCard, HttpStatus.OK);
  }

  
  @GetMapping(value = "/normalschedulecard/dummy", produces = "application/json; charset=utf-8")
  public ResponseEntity<Object> getNormalScheduleCardDummy(@RequestHeader("token") String token) {

    String userId = JWTUtil.getJWTPayloadField(token, "userID");
    
    log.info("Normal dummy service. userId : {}", userId);

    ScheduleCardOutputDTO normalScheduleCard = scheduleMvc.getScheduleCardDummy(userId);

    log.info("Successfully return normal dummy cards. ");

    return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
  }
}
