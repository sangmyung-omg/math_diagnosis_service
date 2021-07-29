package com.tmax.WaplMath.Recommend.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.UserBasicInfoDTO;
import com.tmax.WaplMath.Recommend.dto.UserExamInfoDTO;
import com.tmax.WaplMath.Recommend.service.userinfo.UserInfoServiceBase;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserInfoControllerV0 {

	@Autowired
	@Qualifier("UserInfoServiceV0")
	private UserInfoServiceBase userInfoMvc;


	@PutMapping(value = "/userexaminfo", produces = "application/json; charset=utf-8")
	ResponseEntity<Object> getExamScheduleCard(@RequestHeader("token") String token,
			@RequestBody UserExamInfoDTO input) {

		String userId = JWTUtil.getUserID(token);

    log.info("User exam info service. userId : {}", userId);

		ResultMessageDTO resultMessage = userInfoMvc.updateExamInfo(userId, input);

		return new ResponseEntity<>(resultMessage, HttpStatus.OK);
	}
	

	@PutMapping(value="/userbasicinfo", produces="application/json; charset=utf-8")
	ResponseEntity<Object> updateBasicInfo(@RequestHeader("token") String token, @RequestBody UserBasicInfoDTO input) {

		String userId = JWTUtil.getUserID(token);

    log.info("User basic info service. userId : {}", userId);

		ResultMessageDTO resultMessage = userInfoMvc.updateBasicInfo(userId, input);

		return new ResponseEntity<>(resultMessage, HttpStatus.OK);
	}
	

	@DeleteMapping(value="/user", produces="application/json;charset=utf-8")
	ResponseEntity<Object> deleteUserInfo(@RequestHeader("token") String token){

		String userId = JWTUtil.getUserID(token);

    log.info("User delete service. userId : {}", userId);

		ResultMessageDTO resultMessage = userInfoMvc.deleteUserInfo(userId);

		return new ResponseEntity<>(resultMessage, HttpStatus.OK);
	}
}
