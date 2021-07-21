package com.tmax.WaplMath.AnalysisReport.util.examscope;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserExamScope;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("AR-ExamScopeUtil")
public class ExamScopeUtil {
    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    private UserExamScopeInfoRepo examScopeRepo;

    @Autowired
    private UserRepo userRepo;

    public List<String> getCurrIdListOfScope(String userID){
        //Get user curriculumList from examscope
        UserExamScope examScope = examScopeRepo.getExamScopeOfUser(userID);

        //output curriculum list
        List<Curriculum> currList = null;

        if(examScope != null){
            List<String> excludeList = examScope.getExceptSubSectionIdList() != null ? 
                                    Arrays.asList(examScope.getExceptSubSectionIdList().split(", ")) : 
                                    Arrays.asList(" ");
            currList = curriculumInfoRepo.getCurriculumInRange(examScope.getStartSubSectionId(), examScope.getEndSubSectionId(), excludeList);


            
        }
        else {
            log.warn("Exam scope doesn't exist for user [" + userID + "]. Creating pseudo-list from curriculum info.");

            Optional<User> userInfo = userRepo.findById(userID);

            if(!userInfo.isPresent()){
                throw new GenericInternalException(ARErrorCode.INVALID_PARAMETER, String.format("User id [%s] is not valid. Check if user is properly submited to server", userID));
            }

            //Get current curriculum ID
            String currentCurrID = userInfo.get().getCurrentCurriculumId();

            //Get all sub curriculum IDs
            currList = curriculumInfoRepo.getCurriculumLikeId(currentCurrID);
        }


        return currList.stream().map(item -> item.getCurriculumId()).collect(Collectors.toList());
    }
}
