package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.util.triton.WAPLScoreTriton;
import com.tmax.WaplMath.Recommend.dto.WaplScoreProbListDTO;
import com.tmax.WaplMath.Recommend.model.knowledge.UserEmbedding;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UserEmbeddingRepository;
import com.tmax.WaplMath.Recommend.util.schedule.WaplScoreManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("AR-WaplScoreServiceV0")
public class WaplScoreServiceV0 implements WaplScoreServiceBaseV0 {
    @Autowired
    WaplScoreManager waplScoreManager;

    @Autowired
    @Qualifier("AR-UserInfoRepo")
    UserInfoRepo userInfoRepo;

    @Autowired
	UserEmbeddingRepository userEmbeddingRepo;

    @Autowired
    WAPLScoreTriton waplScoreTriton;

    Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public WAPLScoreDTO getWaplScore(String userID) {
        // TODO Auto-generated method stub

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

        WAPLScoreDTO output = new WAPLScoreDTO((float)100*score/count, (float)0.0);


        return output;
    }
}
