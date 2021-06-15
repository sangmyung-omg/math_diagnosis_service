package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import java.util.ArrayList;
import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.util.curriculum.CurriculumHelper;
import com.tmax.WaplMath.AnalysisReport.util.curriculum.SchoolData;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service("AR-StudyGuideServiceV1")
public class StudyGuideServiceV1 implements StudyGuideServiceBase{

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    UserInfoRepo userInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    UserExamScopeInfoRepo examScopeRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    UserKnowledgeRepo userKnowledgeRepo;


    @Override
    public StudyGuideDTO getStudyGuideOfUser(@NonNull String userID) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException();
        }

        //Get user info from Repo
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        if(userInfo == null) {
            throw new GenericInternalException("ERR-0005", "Can not find valid user Info");
        }
        
        //Get the current target curriculum of user (semester)
        //Extract the grade and semester from the user info
        int grade = Integer.parseInt(userInfo.getGrade());
        int semester = Integer.parseInt(userInfo.getSemester());

        //TODO: temp. change schooltype to a proper method
        //guess school type
        int schoolType = CurriculumHelper.getSchoolTypeFromPrefix(userInfo.getCurrentCurriculumId().substring(0,2));

        SchoolData currentSchoolData = new SchoolData(schoolType, grade, semester);

        String currId = CurriculumHelper.getCurriculumID(currentSchoolData);




        //Step 1: from the curriculumId (semester-wise) --> get all chapter list
        List<Curriculum> resultList = currInfoRepo.getSectionsLikeId(currId);

        System.out.println(resultList.toString());
        System.out.println(resultList.size());

        //Build CurrIDList
        List<String> currIDList = new ArrayList<>();
        resultList.forEach(curr -> currIDList.add(curr.getCurriculumId()));
    
        //Get user's knowledge data by the curr range
        List<UserKnowledge> knowledgeList = userKnowledgeRepo.getKnowledgeOfCurrLike(userID, currId);

        //Build chapter tree

        System.out.println(knowledgeList.size());

        //output dto set
        StudyGuideDTO output = new StudyGuideDTO();
        // output.setChapterDetailList(chapterDetailList);
        output.setCommentary(userInfo.getName());

        return output;
    }
}
