package com.tmax.WaplMath.Recommend.event.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for user info related events
 */
@Component
public class UserInfoEventPublisher {
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
    public void publishExamScopeChangeEvent(final String userID) {
        logger.info("Publish user exam scope change event for user: " + userID);
        applicationEventPublisher.publishEvent(new ExamScopeChangeEvent(userID));
    }
    
    public void publishSchoolInfoChangeEvent(final String userID) {
        logger.info("Publish user school info change event for user: " + userID);
        applicationEventPublisher.publishEvent(new SchoolInfoChangeEvent(userID));
    }
	
}
