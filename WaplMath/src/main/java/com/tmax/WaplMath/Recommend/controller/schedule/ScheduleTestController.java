package com.tmax.WaplMath.Recommend.controller.schedule;

import com.tmax.WaplMath.Recommend.config.RecommendConstants;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleCardOutputDTO;
import com.tmax.WaplMath.Recommend.service.schedule.ScheduleServiceBaseV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = RecommendConstants.apiPrefix + "/debug")
public class ScheduleTestController {

  @Autowired
  @Qualifier("ScheduleServiceV2")
  private ScheduleServiceBaseV2 scheduleMvc;


  @GetMapping(value = "/normalschedulecard", produces = "application/json; charset=utf-8")
  public ResponseEntity<Object> getNormalScheduleCard(@RequestParam("userID") String userID){
    log.info("Normal card service. (debugging) userID : {}", userID);

    ScheduleCardOutputDTO normalScheduleCard = scheduleMvc.getNormalScheduleCard(userID, true);

    log.info("Successfully return normal cards. ");

    return new ResponseEntity<>(normalScheduleCard, HttpStatus.OK);
  }


  @GetMapping(value = "/examschedulecard", produces = "application/json; charset=utf-8")
  public ResponseEntity<Object> getExamScheduleCard(@RequestParam("userID") String userID){
    log.info("Exam card service. (debugging) userID : {}", userID);

    ScheduleCardOutputDTO examScheduleCard = scheduleMvc.getExamScheduleCard(userID, true);

    log.info("Successfully return exam cards. ");

    return new ResponseEntity<>(examScheduleCard, HttpStatus.OK);
  }
}
