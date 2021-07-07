package com.tmax.WaplMath.AnalysisReport.util.examscope;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.user.UserExamScope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("AR-ExamScopeUtil")
public class ExamScopeUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    private UserExamScopeInfoRepo examScopeRepo;

    public List<String> getCurrIdListOfScope(String userID){
        //Get user curriculumList from examscope
        UserExamScope examScope = examScopeRepo.getExamScopeOfUser(userID);
        List<String> excludeList = examScope.getExceptSubSectionIdList() != null ? 
                                   Arrays.asList(examScope.getExceptSubSectionIdList().split(", ")) : 
                                   Arrays.asList(" ");
        List<Curriculum> currList = curriculumInfoRepo.getCurriculumInRange(examScope.getStartSubSectionId(), examScope.getEndSubSectionId(), excludeList);


        return currList.stream().map(item -> item.getCurriculumId()).collect(Collectors.toList());
    }
}
