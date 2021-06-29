package com.tmax.WaplMath.Recommend.event.mastery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for Mastery related events
 */
@Component
public class MasteryEventPublisher{
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    
    /**
     * publisher for mastery change event
     * @param userID The userID of whom the update has occured
     */
    public void publishChangeEvent(final String userID){
        logger.info("Publish mastery change event for user: " + userID);
        applicationEventPublisher.publishEvent(new MasteryChangeEvent(userID));
    }
}
