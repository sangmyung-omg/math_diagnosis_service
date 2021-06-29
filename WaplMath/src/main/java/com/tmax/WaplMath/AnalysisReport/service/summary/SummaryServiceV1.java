package com.tmax.WaplMath.AnalysisReport.service.summary;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceV1;
import com.tmax.WaplMath.AnalysisReport.util.curriculum.CurriculumHelper;
import com.tmax.WaplMath.AnalysisReport.util.curriculum.SchoolData;
import com.tmax.WaplMath.AnalysisReport.util.temp.CommentaryGenerator;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 * Summary service v1 implementation
 * @author Jonghyun Seong
 */
@Service("SummaryServiceV1")
public class SummaryServiceV1 implements SummaryServiceBase {

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    UserInfoRepo userInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    UserExamScopeInfoRepo examScopeRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    UserKnowledgeRepo knowledgeRepo;

    @Autowired
    ChapterServiceV1 chapterSvcv1;

    
    @Override
    public SummaryReportDTO getSummaryOfUser(String userID) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException();
        }


        //Get user info from Repo
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        if(userInfo == null) {
            throw new GenericInternalException("ERR-0005", "Can not find valid user Info");
        }


        //Extract the grade and semester from the user info
        int grade = Integer.parseInt(userInfo.getGrade());
        int semester = Integer.parseInt(userInfo.getSemester());

        //TODO: temp. change schooltype to a proper method
        //guess school type
        int schoolType = CurriculumHelper.getSchoolTypeFromPrefix(userInfo.getCurrentCurriculumId().substring(0,2));

        SchoolData currentSchoolData = new SchoolData(schoolType, grade, semester);

        //TODO: examscope is not yet ready
        // Optional<UserExamScope> examScope = Optional.ofNullable(examScopeRepo.getExamScopeOfUser("박평우"));
        // System.out.println("current: " + currentSchoolData.toString() + ", scope: " + examScope.toString());


        //TODO: change to valid target semester
        SchoolData targetSchoolData = CurriculumHelper.increaseSemester(currentSchoolData);
        String targetCurrId = CurriculumHelper.getCurriculumID(targetSchoolData);

          //Get the User knowledge info and create a ukID-mastery map
        List<UserKnowledge> knowledgeList = knowledgeRepo.getUserKnowledge(userID);
        Map<Integer, Float> userUkMasteryMap = new HashMap<>();
        for(UserKnowledge knowledge: knowledgeList){
            //Map only the UKs in range of target curriculum
            if(knowledge.getUk().getCurriculumId().startsWith(targetCurrId))
                userUkMasteryMap.put(knowledge.getUkId(), knowledge.getUkMastery()); 
        }


        //==== Calculate percentile of everyone ==========

        //get Average uk mastery data
        Path path = null;
        try {
            path = ResourceUtils.getFile("classpath:uk_min_max.json").toPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);
        JsonObject min = result.get("min").getAsJsonObject();
        JsonObject max = result.get("max").getAsJsonObject();


        //Calculate the numbers
        int count = 0;
        double scoreUser, scoreMin, scoreMax;
        scoreUser = scoreMin = scoreMax = 0.0;
        for(Map.Entry<Integer, Float> entry: userUkMasteryMap.entrySet()){
            String ukId = entry.getKey().toString();

            scoreMin += min.get(ukId).getAsDouble();
            scoreMax += max.get(ukId).getAsDouble();
            scoreUser+= entry.getValue();

            count++;
        }

        double userScore = 100*scoreUser / count;
        double percentile = (scoreUser > scoreMax) ? 100.0 : 100*( 100 * (scoreUser - scoreMin)/(scoreMax - scoreMin) ) / count; //If user is more than max. then you are the winner
        
        //Get commentary
        String targetExam = userInfo.getCurrentCurriculumId();
        String currentCurriculum = String.format("중등-중%s-%s학", userInfo.getGrade(), userInfo.getSemester());
        List<ChapterDetailDTO> resultList = chapterSvcv1.getChapterListOfUserInRange(userInfo.getUserUuid(), "year", currentCurriculum + "*partinc");
        //sort list
        resultList.sort((a,b) -> new Double(a.getSkillData().getUser()).compareTo(b.getSkillData().getUser()) );

        Set<String> lowList = new HashSet<>();
        Set<String> highList = new HashSet<>();

        if(resultList.size() <= 1) {
            lowList = null;
            highList = null;
        }
        else if(resultList.size() > 4){
            //Two from each side
            lowList.add(resultList.get(0).getName());
            lowList.add(resultList.get(1).getName());

            highList.add(resultList.get(resultList.size() - 2).getName());
            highList.add(resultList.get(resultList.size() - 1).getName());
        }
        else {
            lowList.add(resultList.get(0).getName());

            highList.add(resultList.get(resultList.size() - 1).getName());
        }
        

        // System.out.println(targetExam + resultList.toString());
        String commentary = CommentaryGenerator.createFromData(userInfo.getName(), userScore, percentile, 60.0, 80.0, lowList, highList);

        return new SummaryReportDTO(userScore, percentile, commentary);
    }
}
