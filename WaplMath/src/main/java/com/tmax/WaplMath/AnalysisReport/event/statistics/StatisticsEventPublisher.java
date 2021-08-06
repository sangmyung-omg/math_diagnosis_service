package com.tmax.WaplMath.AnalysisReport.event.statistics;

import com.tmax.WaplMath.Common.util.kafka.*;

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

        //Publish kafka event too
        KafkaPublisher.getInstance().publishMessage(KafkaEvent.builder()
                                                                .eventType(KafkaEventType.USER_EVENT)
                                                                .eventCode(StatisticsUpdateRequestEvent.class.getSimpleName())
                                                                .eventLevel(KafkaEventLevel.INFO)
                                                                .eventMsg("StatisticsUpdateRequestEvent event")
                                                                .sourceUser(userID)
                                                                .build()
                                                                );
    }

    /**
     * Wapl score gen event to invoke waplscore gen or regeneration
     */
    public void publishWaplScoreGenEvent(final String userID, final boolean isForceUpdate){
        log.info("publish wapl score gen event {} {}", userID, isForceUpdate);
        applicationEventPublisher.publishEvent(new WaplScoreGenEvent(userID, isForceUpdate));

        //Publish kafka event too
        KafkaPublisher.getInstance().publishMessage(KafkaEvent.builder()
                                                                .eventType(KafkaEventType.USER_EVENT)
                                                                .eventCode(WaplScoreGenEvent.class.getSimpleName())
                                                                .eventLevel(KafkaEventLevel.INFO)
                                                                .eventMsg("WaplScoreGenEvent event. " + isForceUpdate)
                                                                .sourceUser(userID)
                                                                .build()
                                                                );
    }
}
