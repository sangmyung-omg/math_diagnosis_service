package com.tmax.WaplMath.AnalysisReport.event.statistics;

import java.util.Map;

import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.Recommend.event.mastery.MasteryChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("StatisticsEventListener")
public class StatisticsEventListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    
    @Autowired
    CurrStatisticsServiceBase currStatSvc;

    @Autowired
    UserStatisticsServiceBase userStatSvc;

    /**
     * Handler to call services that run on mastery change
     * @param event
     */
    // @Async
    @EventListener
    public void handleUserMasteryUpdateEvent(MasteryChangeEvent event) {
        //Get user ID
        String userID = event.getUserID();

        Map<String, Float> map = currStatSvc.getCurriculumMasteryOfUser(userID);
        String jsonStr = new Gson().toJson(map);

        logger.info("Updating user_mastery_stats for user: " + userID);
        userStatSvc.updateSpecificUser(userID);
        
        logger.info("Updating curr_mastery for user: " + userID);
        userStatSvc.updateCustomUserStat(userID, "curr_mastery", Statistics.Type.JSON, jsonStr);

        logger.info("Updated all stats for user: " + userID);
    }

}
