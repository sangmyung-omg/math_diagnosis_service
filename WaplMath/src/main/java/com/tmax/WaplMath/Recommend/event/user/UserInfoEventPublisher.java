package com.tmax.WaplMath.Recommend.event.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher for user info related events
 */
@Component
@Slf4j
public class UserInfoEventPublisher {
	
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
    public void publishExamScopeChangeEvent(final String userID) {
        log.info("Publish user exam scope change event for user: " + userID);
        applicationEventPublisher.publishEvent(new ExamScopeChangeEvent(userID));
    }
    
    public void publishSchoolInfoChangeEvent(final String userID) {
        log.info("Publish user school info change event for user: " + userID);
        applicationEventPublisher.publishEvent(new SchoolInfoChangeEvent(userID));
    }
	
}
