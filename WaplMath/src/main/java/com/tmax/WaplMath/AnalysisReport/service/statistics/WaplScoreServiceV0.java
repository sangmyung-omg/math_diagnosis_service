package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.AnalysisReport.util.examscope.ExamScopeUtil;
import com.tmax.WaplMath.AnalysisReport.util.triton.WAPLScoreTriton;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTO;
import com.tmax.WaplMath.Recommend.model.knowledge.UserEmbedding;
import com.tmax.WaplMath.Recommend.model.uk.Uk;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UkRepository;
import com.tmax.WaplMath.Recommend.repository.UserEmbeddingRepository;
import com.tmax.WaplMath.Recommend.util.schedule.WaplScoreManagerV1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
class ScoreMastery {
    private Float score;
    private String masteryJson;
}

@Service("AR-WaplScoreServiceV0")
public class WaplScoreServiceV0 implements WaplScoreServiceBaseV0 {
    @Autowired
    private WaplScoreManagerV1 waplScoreManager;

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    private UserInfoRepo userInfoRepo;

    @Autowired
	private UserEmbeddingRepository userEmbeddingRepo;

    @Autowired
    private WAPLScoreTriton waplScoreTriton;

    @Autowired
    @Qualifier("UserStatisticsServiceV0")
    private UserStatisticsServiceBase userStatSvc;

    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @Autowired
    private UkRepository ukRepo;

    @Autowired
    @Qualifier("CurrStatisticsServiceV0")
    private CurrStatisticsServiceBase currStatSvc;

    @Autowired
    private ExamScopeUtil examScopeUtil;

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public WAPLScoreDTO getWaplScore(String userID) {

        //Check statistics table if wapl score exists
        Statistics existingScore = userStatSvc.getUserStatistics(userID, STAT_WAPL_SCORE);

        //If none found. get new score
        Float waplScore = 0.0f;
        if(existingScore == null){
            logger.info("Generating new WAPL Score");
            waplScore = generateWaplScore(userID).getScore();
        }
        else {
            logger.info("Using existing wapl score:" + existingScore.toString());
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
        Statistics waplMasteryStat = userStatSvc.getUserStatistics(userID, STAT_WAPL_SCORE_MASTERY);
        String jsonData = null;

        //If stat does not exist invoke the generator
        if(waplMasteryStat != null){
            jsonData = waplMasteryStat.getData();
        }
        else {
            logger.info(String.format("WAPL Score mastery for user %s not found. Invoking waplscore gen", userID));
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
     * @param userID
     * @return
     */
    private ScoreMastery generateWaplScore(String userID){
        //Get the target data from the appripriate DB
        User userInfo = userInfoRepo.getUserInfoByUUID(userID);

        //Get info from userInfo
        Timestamp dueDateTS = userInfo.getExamDueDate();
        String currentCurrID = userInfo.getCurrentCurriculum().getCurriculumId();

        //Ex check
        LocalDateTime dueDate = dueDateTS != null ? dueDateTS.toLocalDateTime() : LocalDateTime.now();

        //Get duration to target date
        Duration timeDiff = Duration.between(dueDate, LocalDateTime.now());
        long diffDay = timeDiff.toDays();

        //target exam info
        String examType = userInfo.getExamType() != null ? userInfo.getExamType() : "final"; 
        String targetexam = String.format("%s-%s-%s", userInfo.getGrade(), userInfo.getSemester(), examType);


        //Get list
        logger.info("Data: " + targetexam + currentCurrID + diffDay);
        WaplScoreProbListDTO probList = waplScoreManager.getWaplScoreProbList(targetexam, currentCurrID, (int)diffDay);


        //Get the userEmbedding data
        UserEmbedding embedding = userEmbeddingRepo.getEmbedding(userID);

        //Build the ukList 
        List<Map<Integer, Float>> ukMasteryMapSeq = waplScoreTriton.calculateFromSequenceList(probList.getProbList() , embedding.getUserEmbedding());


        //Calculate score (uk average)
        Map<Integer, Float> firstMap = ukMasteryMapSeq.get(0);
        
        float score = (float)0.0f;
        int count = 0;
        for(Map.Entry<Integer, Float> entry : firstMap.entrySet()){
            score += entry.getValue();
            count++;
        }


        //Save the wapl score to stats
        Float waplScore = score/count;
        userStatSvc.updateCustomUserStat(userID, STAT_WAPL_SCORE, Statistics.Type.FLOAT, waplScore.toString());

        //Convert mastery map to Json with Gson
        String masteryJson = new Gson().toJson(ukMasteryMapSeq);
        userStatSvc.updateCustomUserStat(userID, STAT_WAPL_SCORE_MASTERY, Statistics.Type.JSON, masteryJson);

        //Log
        logger.info("Save to wapl score to table for user: " + userID);

        return new ScoreMastery(waplScore,masteryJson);
    }
}
