package com.tmax.WaplMath.AnalysisReport.event.statistics;

import java.time.Duration;
// import java.util.concurrent.TimeUnit;

import com.tmax.WaplMath.AnalysisReport.service.diagnosis.DiagnosisServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceV0;
import com.tmax.WaplMath.AnalysisReport.util.statistics.IScreamEduDataReader;
import com.tmax.WaplMath.Common.util.shedlock.ShedLockUtil;
import com.tmax.WaplMath.Recommend.event.mastery.MasteryChangeEvent;
import com.tmax.WaplMath.Recommend.event.user.UserDeleteEvent;
import com.tmax.WaplMath.Recommend.event.user.ExamScopeChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
// import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
// import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Component("StatisticsEventListener")
public class StatisticsEventListener {
    
    @Autowired private CurrStatisticsServiceBase currStatSvc;
    @Autowired private UserStatisticsServiceBase userStatSvc;
    @Autowired private UKStatisticsServiceBase ukStatSvc;
    @Autowired private IScreamEduDataReader iScreamEduDataReader;
    @Autowired private ShedLockUtil shedLockUtil;

    @Autowired private DiagnosisServiceBase diagSvc;
    @Autowired private WaplScoreServiceV0 waplScoreSvc;

    /**
     * Handler to call services that run on mastery change
     * @param event
     */
    // @Async
    @EventListener
    public void handleUserMasteryUpdateEvent(MasteryChangeEvent event) {
        //Get user ID
        String userID = event.getUserID();

        log.debug("Updating user_mastery_stats for user {}. {}", userID, "MasteryChangeEvent");
        userStatSvc.updateSpecificUser(userID, true);

        log.debug("Updated all stats for user: " + userID);
    }

    @EventListener
    public void updateUserStatistics(StatisticsUpdateRequestEvent event){
        String userID = event.getUserID();

        if(userID != null){
            log.debug("Updating user Stat for {}. {}", userID, "StatisticsUpdateRequestEvent");
            userStatSvc.updateSpecificUser(userID, true);
        }
    }

    @EventListener
    // @Async
    public void updatedExamScope(ExamScopeChangeEvent event){
        String userID = event.getUserID();

        //Clear waplscore and diagscore stats
        diagSvc.clearDiagnosisStats(userID);
        waplScoreSvc.clearWaplScoreStatistics(userID);

        if(userID != null){
            log.debug("Updating user Stat for {}. {}",userID,"ExamScopeChangeEvent");
            userStatSvc.updateSpecificUser(userID, true);
        }
    }

    /**
     * Startup statistics initializer
     * @param event
     */
    @EventListener
    public void startupStatisticsProcessor(ApplicationStartedEvent event){
        //load iscream edu data
        iScreamEduDataReader.loadData();


        log.info("Checking statistics status");
        boolean result = currStatSvc.updateStatistics(false);

        if(result){
            log.info("Updating Statistics stats");
            ukStatSvc.updateAllStatistics();

            log.info("Updating All user stats");
            userStatSvc.updateAllUsers();     
        }
        
        log.info("Statistics module ready");
    }

    @EventListener
    public void userDeleteStatisticCleaner(UserDeleteEvent event){
        String userID = event.getUserID();

        if(userID == null) return;

        log.info("Delete stats for user {}", userID);
        userStatSvc.clearAllUserStatistics(userID);
    }

    @Scheduled(cron="0 0 0 * * *")
    // @SchedulerLock(name="Stat_Update_Curriculum", lockAtLeastFor = "PT5M")
    public void updateCurriculumStats(){
        String lockname = "Stat_Update_Curriculum";
        if(!shedLockUtil.tryLock(lockname, Duration.ofMinutes(30)) ){
            log.info("Schedule run for {} is already taken by another cluster", lockname);
            return;
        }


        log.info("======= Nightly Curr statistics update START ========");
        // log.info("Curriculum");
        currStatSvc.updateStatistics();
        log.info("======= Nightly statistics update DONE ========");   

        //Release lock
        shedLockUtil.releaseLock(lockname);
    }

    @Scheduled(cron="0 0 0 * * *")
    // @SchedulerLock(name="Stat_Update_User", lockAtLeastFor = "PT5M")
    public void updateUserStats(){
        String lockname = "Stat_Update_User";
        if(!shedLockUtil.tryLock(lockname, Duration.ofMinutes(30)) ){
            log.info("Schedule run for {} is already taken by another cluster", lockname);
            return;
        }


        log.info("======= Nightly User statistics update START ========");
        // log.info("User");
        userStatSvc.updateAllUsers();     
        log.info("======= Nightly User statistics update DONE ========");
        
        
        //Release lock
        shedLockUtil.releaseLock(lockname);
    }

    @Scheduled(cron="0 0 0 * * *")
    // @SchedulerLock(name="Stat_Update_UK", lockAtLeastFor = "PT5M")
    public void updateUkStats(){
        String lockname = "Stat_Update_UK";
        if(!shedLockUtil.tryLock(lockname, Duration.ofMinutes(30)) ){
            log.info("Schedule run for {} is already taken by another cluster", lockname);
            return;
        }
        
        log.info("======= Nightly UK statistics update START ========");
        // log.info("UK");
        ukStatSvc.updateAllStatistics();    
        log.info("======= Nightly UK statistics update DONE ========");   

        //Release lock
        shedLockUtil.releaseLock(lockname);
    }

    @EventListener
    public void generateWaplScoreListener(WaplScoreGenEvent event){
        log.info("Gen waplscore {} force = {}", event.getUserID(), event.isForceGen());
        waplScoreSvc.generateWaplScore(event.getUserID(), event.isForceGen());
    }
}
