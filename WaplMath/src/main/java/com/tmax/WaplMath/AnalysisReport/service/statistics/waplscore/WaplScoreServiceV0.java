package com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.AnalysisReport.util.triton.WAPLScoreTriton;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.knowledge.UserEmbedding;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTO;
import com.tmax.WaplMath.Recommend.repository.UkRepo;
import com.tmax.WaplMath.Recommend.repository.UserEmbeddingRepo;
import com.tmax.WaplMath.Recommend.util.waplscore.WaplScoreManagerV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("AR-WaplScoreServiceV0")
public class WaplScoreServiceV0 implements WaplScoreServiceBaseV0 {
    @Autowired private WaplScoreManagerV1 waplScoreManager;

    @Autowired @Qualifier("AR-UserInfoRepo")
    private UserInfoRepo userInfoRepo;

    @Autowired @Qualifier("RE-UserEmbeddingRepo")
    private UserEmbeddingRepo userEmbeddingRepo;

    @Autowired private WAPLScoreTriton waplScoreTriton;

    @Autowired @Qualifier("UserStatisticsServiceV0")
    private UserStatisticsServiceBase userStatSvc;

    @Autowired @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired @Qualifier("RE-UkRepo")
    private UkRepo ukRepo;

    @Autowired @Qualifier("CurrStatisticsServiceV0")
    private CurrStatisticsServiceBase currStatSvc;

    @Autowired private ExamScopeUtil examScopeUtil;

    @Override
    public int clearWaplScoreStatistics(String userID) {
        boolean succeed = userStatSvc.clearUserStatistics(userID, Arrays.asList(UserStatisticsServiceBase.STAT_WAPL_SCORE, UserStatisticsServiceBase.STAT_WAPL_SCORE_MASTERY));
        return succeed ? 1 : 0;
    }

    @Override
    public WAPLScoreDTO getWaplScore(String userID) {

        //Check statistics table if wapl score exists
        Statistics existingScore = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE);

        //If none found. get new score
        Float waplScore = 0.0f;
        if(existingScore == null){
            log.info("Generating new WAPL Score for user {}", userID);
            waplScore = generateWaplScore(userID).getScore();
        }
        else {
            log.info("Using existing wapl score: {} : {}", userID, existingScore.toString());
            waplScore = Float.valueOf(existingScore.getData());
        }
        

        //Calculate percentile of the score
        //Get curriculum data
        List<Float> sortedMasteryList = getSortedMasteryListOfCurr(userID);
        Float percentile = ukStatSvc.getPercentile(waplScore, sortedMasteryList);  

        return new WAPLScoreDTO(waplScore, percentile);
    }

    @Override
    public WAPLScoreDTO getCurriculumWaplScore(String userID, String currID) {
        //Get the wapl score mastery info
        Map<Integer, Float> waplMasteryMap = getWaplMasteryMap(userID);

        //Get the ukID List for the given currID
        List<Uk> ukList = ukRepo.findByLikelyCurriculumId(currID);


        Float waplScore = 0.0f;
        //For all ukID, get data from mastery map then calc average
        for(Uk uk: ukList){
            //If the map doesn't have the uk key
            if(!waplMasteryMap.containsKey(uk.getUkId()))
                continue;

            waplScore += waplMasteryMap.get(uk.getUkId());
        }
        //Make average
        if(ukList.size() > 0)
            waplScore /= (float)ukList.size();

        //Calculate percentile
        List<Float> sortedMasteryList = getSortedMasteryListOfCurr(userID);
        Float percentile = ukStatSvc.getPercentile(waplScore, sortedMasteryList); 

        return new WAPLScoreDTO(waplScore, percentile);
    }

    private List<Float> getSortedMasteryListOfCurr(String userID){
        //Get curriculum data
        Statistics sortedStat =  currStatSvc.getCoarseAverageStatistics(examScopeUtil.getCurrIdListOfScope(userID), 
                                                                        CurrStatisticsServiceBase.STAT_MASTERY_SORTED);
        Type floatType = new TypeToken<List<Float>>(){}.getType();
        return new Gson().fromJson(sortedStat.getData(), floatType);
    }

    private Map<Integer, Float> getWaplMasteryMap(String userID){
        //Declare and init output map
        Map<Integer, Float> output = null;

        //Try and get cache
        Statistics waplMasteryStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE_MASTERY);
        String jsonData = null;

        //If stat does not exist invoke the generator
        if(waplMasteryStat != null){
            jsonData = waplMasteryStat.getData();
        }
        else {
            log.warn("WAPL Score mastery for user {} not found. Invoking waplscore gen", userID);
            jsonData = generateWaplScore(userID).getMasteryJson();
        }
        

        //Parse the mastery to stats. 1) to List<Map<Integer, Float>>
        try {
            Type type = new TypeToken<List<Map<Integer, Float>>>(){}.getType();
            List<Map<Integer, Float>> masteryMapList = new Gson().fromJson(jsonData, type);

            //If the list has more than 1 element, get the first element and set as output.
            if(masteryMapList.size() > 0)
                output = masteryMapList.get(0);
        }
        catch(Throwable e){
            throw new GenericInternalException(ARErrorCode.JSON_PROCESSING_ERROR,"JSON Parse error while processing masteryMap list");
        }


        if(output == null)
            throw new GenericInternalException(ARErrorCode.INVALID_MASTERY_DATA, "No mastery data found.");

        return output;        
    }

    
    /**
     * Generate new waplscore
     * @param userID userID of user to calc wapl score
     * @param saveToDB option to save stat data direct to database
     * @return
     */
    public WaplScoreData generateWaplScore(String userID, boolean saveToDB){
        return generateWaplScore(userID, null, saveToDB);
    }
    public WaplScoreData generateWaplScore(String userID, Float examScopeScore, boolean saveToDB){
        //Get the target data from the appripriate DB
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        //Get info from userInfo
        Timestamp dueDateTS = userInfo.getExamDueDate();
        String currentCurrID = userInfo.getCurrentCurriculum().getCurriculumId();

        //Ex check
        LocalDateTime dueDate = dueDateTS != null ? dueDateTS.toLocalDateTime() : LocalDateTime.now();

        //Get duration to target date
        Duration timeDiff = Duration.between(LocalDateTime.now(), dueDate);
        long diffDay = Math.max(1, timeDiff.toDays()); // bound diffday to at least 1 day

        //target exam info
        String examType = userInfo.getExamType() != null ? userInfo.getExamType() : "final"; 
        String targetexam = String.format("%s-%s-%s", userInfo.getGrade(), userInfo.getSemester(), examType);


        //Get list
        log.info("Data: " + targetexam + currentCurrID + diffDay);
        WaplScoreProbListDTO probList = waplScoreManager.getWaplScoreProbList(targetexam, currentCurrID, (int)diffDay);


        //Get the userEmbedding data
        UserEmbedding embedding = userEmbeddingRepo.getEmbedding(userID);
        String embeddingData = embedding != null ? embedding.getUserEmbedding() : "";

        //Build the ukList 
        List<Map<Integer, Float>> ukMasteryMapSeq = waplScoreTriton.calculateFromSequenceList(probList.getProbList() , embeddingData);


        //Calculate score (uk average)
        if(ukMasteryMapSeq.size() == 0){
            log.error("Invalid WAPL score mastery. Check triton response. {}", userID);
            throw new GenericInternalException(ARErrorCode.WAPL_SCORE_TRITON_DATA_ERROR,
                                               String.format("triton input data = [%s, %s]", probList.getProbList(), embeddingData));
        }

        //Get last map
        Map<Integer, Float> lastMap = ukMasteryMapSeq.get(ukMasteryMapSeq.size() - 1);
        
        float score = (float)0.0f;
        int count = 0;
        for(Map.Entry<Integer, Float> entry : lastMap.entrySet()){
            score += entry.getValue();
            count++;
        }


        //Save the wapl score to stats
        Double waplScore = (double)score/count;

        //Exam score exception test
        Double examScore = null;
        if(examScopeScore == null){
            //Fix waplScore if it is lower than examScore
            Statistics examScoreStat = userStatSvc.getUserStatistics(userID, UserStatisticsServiceBase.STAT_EXAMSCOPE_SCORE);
            if(examScoreStat != null){
                examScore = (double)examScoreStat.getAsFloat();
            }
        }
        else
            examScore = (double)examScopeScore;

        if(waplScore < examScore){
            log.warn("Wapl Score calibration for {}", userID);
            double diff = 1.0f - examScore;
            waplScore = Math.min(1.0,examScore + Math.random()*diff);
            log.warn("Calibrated with exam :{} wapl: {} for {}", examScore, waplScore, userID);
        }

        //Convert mastery map to Json with Gson
        String masteryJson = new Gson().toJson(ukMasteryMapSeq);

        if(saveToDB){
            userStatSvc.updateCustomUserStat(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE, Statistics.Type.FLOAT, waplScore.toString());
            userStatSvc.updateCustomUserStat(userID, UserStatisticsServiceBase.STAT_WAPL_SCORE_MASTERY, Statistics.Type.JSON, masteryJson);
        }

        //Log
        log.info("Save to wapl score to table for user: " + userID);

        return new WaplScoreData(waplScore.floatValue(),masteryJson);
    }

    public WaplScoreData generateWaplScore(String userID){
        return generateWaplScore(userID, true);
    }
}
