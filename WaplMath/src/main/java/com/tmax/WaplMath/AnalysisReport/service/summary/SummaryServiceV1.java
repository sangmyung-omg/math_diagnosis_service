package com.tmax.WaplMath.AnalysisReport.service.summary;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SummaryReportDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserExamScopeInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceV1;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.AnalysisReport.util.temp.CommentaryGenerator;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Recommend.model.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Summary service v1 implementation
 * @author Jonghyun Seong
 */
@Service("SummaryServiceV1")
public class SummaryServiceV1 implements SummaryServiceBase {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    private UserInfoRepo userInfoRepo;

    @Autowired
    @Qualifier("AR-UserExamScopeInfoRepo")
    private UserExamScopeInfoRepo examScopeRepo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    private UserKnowledgeRepo knowledgeRepo;

    @Autowired
    private ChapterServiceV1 chapterSvcv1;

    @Autowired
    @Qualifier("CurrStatisticsServiceV0")
    private CurrStatisticsServiceBase currStatSvc;

    @Autowired
    @Qualifier("UserStatisticsServiceV0")
    private UserStatisticsServiceBase userStatSvc;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    @Qualifier("AR-CurriculumInfoRepo")
    private CurriculumInfoRepo curriculumInfoRepo;

    @Autowired
    @Qualifier("AR-ExamScopeUtil")
    private ExamScopeUtil examScopeUtil;

    
    @Override
    public SummaryReportDTO getSummaryOfUser(String userID) {
        //Exception handling for input parameters
        if(userID == null){
            throw new InvalidArgumentException();
        }


        //Get user info from Repo
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        if(userInfo == null) {
            throw new GenericInternalException("ERR-0005", "Can not find valid user Info for " + userID);
        }

        //Declare all variables
        Float userScore = -1.0f;
        Float percentile = -1.0f;
        Float targetpercentile =  -1.0f;
        Float average =  -1.0f;
        Float std =  -1.0f;
        String commentary = null;

        //Get examscope userScore from stat table
        try {
            userScore = Float.valueOf(userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_SCORE).getData());
        }
        catch(Throwable e){
            logger.warn("No EXAMSCOPE SCORE found for user " + userID);
        }

        //Get curriculum data
        if(userScore >= 0){
            List<String> examScopeCurrIDList = examScopeUtil.getCurrIdListOfScope(userID);
            List<Float> sortedMasteryList = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList, 
                                                                                CurrStatisticsServiceV0.STAT_MASTERY_SORTED).getAsFloatList();
            percentile = ukStatSvc.getPercentile(userScore, sortedMasteryList);
            
            //
            if(userInfo.getExamTargetScore() != null)
                targetpercentile = ukStatSvc.getPercentile((float)userInfo.getExamTargetScore()/100, sortedMasteryList);
            else
                targetpercentile = -1.0f;

            //Get std, average
            average = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList, 
                                                                CurrStatisticsServiceV0.STAT_MASTERY_MEAN).getAsFloat();
            std = currStatSvc.getCoarseAverageStatistics(examScopeCurrIDList, 
                                                            CurrStatisticsServiceV0.STAT_MASTERY_STD).getAsFloat();

            // System.out.println(targetExam + resultList.toString());
            commentary = getCommentary(userInfo, 100*userScore, 100*percentile, 60.0f, 80.0f);
        }

        return SummaryReportDTO.builder()
                               .score(100*userScore)
                               .percentile(100*percentile)
                               .targetpercentile(100*targetpercentile)
                               .average(100*average)
                               .std(100*std)
                               .commentary(commentary)
                               .build();
    }

    /**
     * Commentary generating method.
     * This method generates a comment based on the given param
     * @param userInfo
     * @param userScore
     * @param percentile
     * @return
     */
    private String getCommentary(User userInfo, Float userScore, Float percentile, Float speed, Float correctRate){
        //Get commentary
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

        return CommentaryGenerator.createFromData(userInfo.getName(), (double)userScore, (double)percentile, (double)speed, (double)correctRate, lowList, highList);
    }

    private Float calculatePercentile(Map<Integer, Float> masteryMap){
        return 0.0f;
    }

    // private void legacyCode() {
    // //Get the User knowledge info and create a ukID-mastery map
    // List<UserKnowledge> knowledgeList = knowledgeRepo.getUserKnowledge(userID);
    // Map<Integer, Float> userUkMasteryMap = new HashMap<>();
    // for(UserKnowledge knowledge: knowledgeList){
    //     //Map only the UKs in range of target curriculum
    //     if(knowledge.getUk().getCurriculumId().startsWith(targetCurrId))
    //         userUkMasteryMap.put(knowledge.getUkId(), knowledge.getUkMastery()); 
    // }


    // //==== Calculate percentile of everyone ==========

    // //get Average uk mastery data
    // Path path = null;
    // try {
    //     path = ResourceUtils.getFile("classpath:uk_min_max.json").toPath();
    // } catch (FileNotFoundException e) {
    //     e.printStackTrace();
    // }

    // FileReader reader = null;
    // try {
    //     reader = new FileReader(path.toString());
    // } catch (FileNotFoundException e) {
    //     e.printStackTrace();
    // }

    // JsonObject result = (JsonObject)JsonParser.parseReader(reader);
    // JsonObject min = result.get("min").getAsJsonObject();
    // JsonObject max = result.get("max").getAsJsonObject();


    // //Calculate the numbers
    // int count = 0;
    // double scoreUser, scoreMin, scoreMax;
    // scoreUser = scoreMin = scoreMax = 0.0;
    // for(Map.Entry<Integer, Float> entry: userUkMasteryMap.entrySet()){
    //     String ukId = entry.getKey().toString();

    //     scoreMin += min.get(ukId).getAsDouble();
    //     scoreMax += max.get(ukId).getAsDouble();
    //     scoreUser+= entry.getValue();

    //     count++;
    // }

    // double userScore = 100*scoreUser / count;
    // double percentile = (scoreUser > scoreMax) ? 100.0 : 100*( 100 * (scoreUser - scoreMin)/(scoreMax - scoreMin) ) / count; //If user is more than max. then you are the winner
    
    // }
}
