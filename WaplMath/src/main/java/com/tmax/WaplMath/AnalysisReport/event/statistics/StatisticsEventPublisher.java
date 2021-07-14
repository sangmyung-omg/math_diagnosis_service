package com.tmax.WaplMath.AnalysisReport.event.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Publisher for Statistics related events
 * @author Jonghyun Seong
 * @since 2021-07-14
 */
@Slf4j
@Component
public class StatisticsEventPublisher {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * publisher for mastery change event
     * @param userID The userID of whom the update has occured
     */
    public void publishChangeEvent(final String userID){
        log.info("Publish mastery change event for user: " + userID);
        applicationEventPublisher.publishEvent(new StatisticsUpdateRequestEvent(userID));
    }
}
