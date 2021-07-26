package com.tmax.WaplMath.AnalysisReport.event.statistics;

import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceV0;
import com.tmax.WaplMath.Recommend.event.mastery.MasteryChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

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

        log.debug("Updating user_mastery_stats for user: " + userID);
        userStatSvc.updateSpecificUser(userID);

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

    @Scheduled(cron="0 0 0 * * *")
    @SchedulerLock(name="Stat_Update_Curriculum", lockAtLeastFor = "PT5M")
    public void updateCurriculumStats(){
        log.info("======= Nightly Curr statistics update START ========");
        log.info("Curriculum");
        currStatSvc.updateStatistics();
        log.info("======= Nightly statistics update DONE ========");   
    }

    @Scheduled(cron="0 0 0 * * *")
    @SchedulerLock(name="Stat_Update_User", lockAtLeastFor = "PT5M")
    public void updateUserStats(){
        log.info("======= Nightly User statistics update START ========");
        log.info("User");
        userStatSvc.updateAllUsers();     
        log.info("======= Nightly User statistics update DONE ========");   
    }

    @Scheduled(cron="0 0 0 * * *")
    @SchedulerLock(name="Stat_Update_UK", lockAtLeastFor = "PT5M")
    public void updateUkStats(){
        log.info("======= Nightly UK statistics update START ========");
        log.info("UK");
        ukStatSvc.updateAllStatistics();    
        log.info("======= Nightly UK statistics update DONE ========");   
    }

    @Autowired
    WaplScoreServiceV0 waplScoreSvc;

    @EventListener
    public void generateWaplScoreListener(WaplScoreGenEvent event){
        log.info("Gen waplscore {} force = {}", event.getUserID(), event.isForceGen());
        waplScoreSvc.generateWaplScore(event.getUserID(), event.isForceGen());
    }
}
