package com.tmax.WaplMath.AnalysisReport.event.statistics;

import java.util.Map;

import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.Recommend.event.mastery.MasteryChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("StatisticsEventListener")
public class StatisticsEventListener {
    
    @Autowired
    CurrStatisticsServiceBase currStatSvc;

    @Autowired
    UserStatisticsServiceBase userStatSvc;

    @Autowired
    UKStatisticsServiceBase ukStatSvc;

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

        log.debug("Updating user_mastery_stats for user: " + userID);
        userStatSvc.updateSpecificUser(userID);
        
        log.debug("Updating curr_mastery for user: " + userID);
        userStatSvc.updateCustomUserStat(userID, "curr_mastery", Statistics.Type.JSON, jsonStr);

        log.debug("Updated all stats for user: " + userID);
    }

    @EventListener
    public void updateUserStatistics(StatisticsUpdateRequestEvent event){
        String userID = event.getUserID();

        if(userID != null){
            log.debug("Updating user Stat for " + userID);
            userStatSvc.updateSpecificUser(userID);
        }
    }



    /**
     * Startup statistics initializer
     * @param event
     */
    @EventListener
    public void startupStatisticsProcessor(ApplicationStartedEvent event){
        log.info("Initializing Curriculum stats");
        boolean result = currStatSvc.updateStatistics(false);

        if(result){
            log.info("Updating Statistics stats");
            ukStatSvc.updateAllStatistics();

            log.info("Updating All user stats");
            userStatSvc.updateAllUsers();     
        }
    }



    //TEMP. FIXME: scheduled statistics updater. every midnight
    @Scheduled(cron="0 0 0 * * *")
    public void updateAllStatistics(){
        log.info("======= Nightly statistics update START ========");
        log.info("Curriculum");
        currStatSvc.updateStatistics();

        log.info("UK");
        ukStatSvc.updateAllStatistics();

        log.info("User");
        userStatSvc.updateAllUsers();     
        log.info("======= Nightly statistics update DONE ========");   
    }

}
