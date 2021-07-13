package com.tmax.WaplMath.Recommend.event.mastery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher for Mastery related events
 */
@Component
@Slf4j
public class MasteryEventPublisher{
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * publisher for mastery change event
     * @param userID The userID of whom the update has occured
     */
    public void publishChangeEvent(final String userID){
        log.info("Publish mastery change event for user: " + userID);
        applicationEventPublisher.publishEvent(new MasteryChangeEvent(userID));
    }
}
