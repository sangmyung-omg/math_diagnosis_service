package com.tmax.WaplMath.Recommend.event.mastery;

import com.tmax.WaplMath.Common.util.kafka.*;

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


        //Publish kafka event too
        KafkaPublisher.getInstance().publishMessage(KafkaEvent.builder()
                                                                .eventType(KafkaEventType.USER_EVENT)
                                                                .eventCode(MasteryChangeEvent.class.getSimpleName())
                                                                .eventLevel(KafkaEventLevel.INFO)
                                                                .eventMsg("Mastery change event occurred")
                                                                .sourceUser(userID)
                                                                .build()
                                                                );
    }
}
