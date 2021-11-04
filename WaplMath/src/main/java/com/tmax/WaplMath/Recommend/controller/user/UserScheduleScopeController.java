package com.tmax.WaplMath.Recommend.controller.user;

import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.user.UserScheduleScopeInDTO;
import com.tmax.WaplMath.Recommend.dto.user.UserScheduleScopeOutDTO;
import com.tmax.WaplMath.Recommend.service.userinfo.UserScheduleScopeBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

// 2021-11-02 Added by Sangheon Lee.
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "")
public class UserScheduleScopeController {

  @Autowired
  @Qualifier("UserScheduleScopeServiceV0")
  UserScheduleScopeBase userScheduleMvc;
 

	@GetMapping(value="/userschedulescope", produces="application/json; charset=utf-8")
	ResponseEntity<Object> getUserScheduleScope(@RequestHeader("token") String token) {

		String userId = JWTUtil.getUserID(token);

    log.info("GET user schedule scope service. userId : {}", userId);

		UserScheduleScopeOutDTO userScheduleScope = userScheduleMvc.getScheduleScope(userId);

    log.info("Successfully return user schedule scope. ");

		return new ResponseEntity<>(userScheduleScope, HttpStatus.OK);
	}


	@PutMapping(value="/userschedulescope", produces="application/json; charset=utf-8")
	ResponseEntity<Object> updateUserScheduleScope(@RequestHeader("token") String token, 
                                                 @RequestBody UserScheduleScopeInDTO input) {

		String userId = JWTUtil.getUserID(token);

    log.info("PUT user schedule scope service. UserId = {}, toDefault = {}, sectionIdList = {}", 
                                        userId, input.getToDefault(), input.getSectionIdList());

		ResultMessageDTO resultMessage = userScheduleMvc.updateScheduleScope(userId, input);

    log.info("Successfully update user schedule scope. ");

		return new ResponseEntity<>(resultMessage, HttpStatus.OK);
	}
}
