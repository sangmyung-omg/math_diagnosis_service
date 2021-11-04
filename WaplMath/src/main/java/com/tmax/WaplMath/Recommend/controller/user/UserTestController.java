package com.tmax.WaplMath.Recommend.controller.user;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserRecommendScope;
import com.tmax.WaplMath.Common.repository.user.UserRecommendScopeRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.service.userinfo.UserInfoServiceBase;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.user.UserInfoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  
  @Autowired
  private UserRepo userRepo;
  
  @Autowired
  private UserRecommendScopeRepo userScopeRepo;

  @Autowired
  private UserInfoManager userInfoManager;
  
  @Autowired
  @Qualifier("RE-CurriculumRepo")
  private CurriculumRepo curriculumRepo;


  @DeleteMapping(value="/users", produces="application/json;charset=utf-8")
	ResponseEntity<Object> deleteUserInfo(@RequestParam("userIDList") List<String> userIDList){
    
    for (String userId: userIDList){

      log.info("User delete service. userId : {}", userId);

      ResultMessageDTO resultMessage = userInfoMvc.deleteUserInfo(userId);
      
      log.info("{}", resultMessage.getMessage());
  
    }
    return new ResponseEntity<>(new ResultMessageDTO("Successfully delete users."), HttpStatus.OK);
  }


	@PutMapping(value="/userschedulescope/init", produces="application/json; charset=utf-8")
	ResponseEntity<Object> updateAllUserScheduleScope() {

    log.info("PUT user schedule scope service. All users set to default scope based on Academic Calendar. ");

    Iterator<User> allUsers = userRepo.findAll().iterator();

    while (allUsers.hasNext()){

      User user = allUsers.next();
      
      String userId = user.getUserUuid();
      String grade = user.getGrade();
      String semester = user.getSemester();
      
      UserRecommendScope userScope = userScopeRepo.findById(userId).orElse(new UserRecommendScope());

      if (userScope.getScheduleScope() == null) {

        log.info("Set user schedule scope. userId={} (grade={}, sem={}) ", userId, grade, semester);

        // today curriculum id based on academic calendar
        String currentCurriculumId = userInfoManager.getCurriculumIdwithCalendar(grade, semester, new Date());

        // 이번 학기 마지막까지
        String endCurriculumId = ExamScope.examScope.get(grade + "-" + semester + "-" + "final").get(1);

        List<String> subSectionIdList = curriculumRepo.findSubSectionListBetween(currentCurriculumId, endCurriculumId);

        userScope.setUserUuid(userId);
        userScope.setScheduleScope(subSectionIdList.toString().replace("[", "").replace("]", ""));
        userScopeRepo.save(userScope);

      }
    }

    log.info("Successfully update all users' schedule scope. ");

		return new ResponseEntity<>(new ResultMessageDTO("Successfully update all users' schedule scope."), HttpStatus.OK);
	}
}
