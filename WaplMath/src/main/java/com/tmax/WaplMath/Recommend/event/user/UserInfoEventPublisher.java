package com.tmax.WaplMath.Recommend.event.user;

import com.tmax.WaplMath.Common.util.kafka.*;

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

        //Publish kafka event too
        KafkaPublisher.getInstance().publishMessage(KafkaEvent.builder()
                                                                .eventType(KafkaEventType.USER_EVENT)
                                                                .eventCode(ExamScopeChangeEvent.class.getSimpleName())
                                                                .eventLevel(KafkaEventLevel.INFO)
                                                                .eventMsg("exam scope change event")
                                                                .sourceUser(userID)
                                                                .build()
                                                                );
    }
    
    public void publishSchoolInfoChangeEvent(final String userID) {
        log.info("Publish user school info change event for user: " + userID);
        applicationEventPublisher.publishEvent(new SchoolInfoChangeEvent(userID));

        //Publish kafka event too
        KafkaPublisher.getInstance().publishMessage(KafkaEvent.builder()
                                                                .eventType(KafkaEventType.USER_EVENT)
                                                                .eventCode(SchoolInfoChangeEvent.class.getSimpleName())
                                                                .eventLevel(KafkaEventLevel.INFO)
                                                                .eventMsg("school info change event")
                                                                .sourceUser(userID)
                                                                .build()
                                                                );
    }
	
}
