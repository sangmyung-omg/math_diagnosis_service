package com.tmax.WaplMath.Recommend.controller.user;

import java.util.List;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.service.userinfo.UserInfoServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "/test")
public class UserTestController {
  
	@Autowired
	@Qualifier("UserInfoServiceV0")
	private UserInfoServiceBase userInfoMvc;
  
  @DeleteMapping(value="/users", produces="application/json;charset=utf-8")
	ResponseEntity<Object> deleteUserInfo(@RequestParam("userIDList") List<String> userIDList){
    
    for (String userId: userIDList){

      log.info("User delete service. userId : {}", userId);

      ResultMessageDTO resultMessage = userInfoMvc.deleteUserInfo(userId);
      
      log.info("{}", resultMessage.getMessage());
  
    }
    return new ResponseEntity<>(new ResultMessageDTO("Successfully delete users."), HttpStatus.OK);
  }
}
