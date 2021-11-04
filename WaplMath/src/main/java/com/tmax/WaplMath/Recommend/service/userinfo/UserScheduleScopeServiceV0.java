package com.tmax.WaplMath.Recommend.service.userinfo;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserRecommendScope;
import com.tmax.WaplMath.Common.repository.user.UserRecommendScopeRepo;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.user.UserScheduleScopeInDTO;
import com.tmax.WaplMath.Recommend.dto.user.UserScheduleScopeOutDTO;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.user.UserInfoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Qualifier("UserScheduleScopeServiceV0")
public class UserScheduleScopeServiceV0 implements UserScheduleScopeBase {

  @Autowired
  private UserRecommendScopeRepo userScopeRepo;

  @Autowired
  @Qualifier("RE-CurriculumRepo")
  private CurriculumRepo curriculumRepo;

  @Autowired
  private UserInfoManager userInfoManager;
  

  public List<String> getScopeWithCalendar(String userId){

    log.info("Set with default scope based on academic calendar.");

    // get valid user information in USER_MASTER tb
    User user = userInfoManager.getValidUserInfo(userId);
    String grade = user.getGrade();
    String semester = user.getSemester();
    
    // today curriculum id based on academic calendar
    String currentCurriculumId = userInfoManager.getCurriculumIdwithCalendar(grade, semester, new Date());

    // 이번 학기 마지막까지
    String endCurriculumId = ExamScope.examScope.get(grade + "-" + semester + "-" + "final").get(1);

    return curriculumRepo.findSubSectionListBetween(currentCurriculumId, endCurriculumId);
  }


  @Override
  public UserScheduleScopeOutDTO getScheduleScope(String userId) {

    // get schedule scope subSection ids
    List<String> subSectionIdList = userInfoManager.getScheduleScopeSubSectionIdList(userId);

    // convert subSection ids to section ids
    Set<String> sectionIdSet = subSectionIdList.stream()
                                               .map(subSectionId -> subSectionId.substring(0, 14))
                                               .collect(Collectors.toSet());
                    
    // sort section id with curriculum sequence
    List<String> sectionIdList = curriculumRepo.sortByCurrSeq(sectionIdSet);
    
    return new UserScheduleScopeOutDTO("Successfully return user schedule scope.", sectionIdList);
  }


  @Override
  @Transactional
  public ResultMessageDTO updateScheduleScope(String userId, UserScheduleScopeInDTO input) {

    if (userScopeRepo.existsById(userId)){
      UserRecommendScope userScope = userScopeRepo.findById(userId).get();
      
      // new user schedule scope
      List<String> subSectionIdList;

      if (input.getToDefault()) {
        // get default scope based on academic calendar
        subSectionIdList = getScopeWithCalendar(userId);
      }

      else {
        List<String> sectionIdList = input.getSectionIdList();

        // toDefault = false then sectionIdList not be null or empty.
        if ( sectionIdList.equals(null) || sectionIdList.isEmpty() || sectionIdList.contains("") ) {

          log.error("sectionIdList must not be null or empty. userId = {}", userId);
          throw new IllegalArgumentException("sectionIdList must not be null or empty.");
        }

        // set schedule scope to given sectionIdList
        subSectionIdList = curriculumRepo.findSubSectionListInSectionSet(new HashSet<>(sectionIdList));

        // return warning if no sub section ids
        if ( subSectionIdList.isEmpty() )
          return new ResultMessageDTO("Warning: No sections where found. sectionIdList may be inappropriate.");
      }

      userScope.setScheduleScope(subSectionIdList.toString().replace("[", "").replace("]", ""));
      userScopeRepo.save(userScope);

      return new ResultMessageDTO("Successfully update user schedule scope.");
    }
    else {
      return new ResultMessageDTO(String.format("Warning: User %s is not in USER_RECOMMEND_SCOPE TB", userId));
    }
  }
  
}
