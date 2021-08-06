package com.tmax.WaplMath.Common.util.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaEvent {
    private KafkaEventType eventType; //Exception / warning etc
    private String eventCode; //subType of the event
    private String eventMsg;
    private String sourceUser;
    private String sourceHost; //source host of event
    private KafkaEventLevel eventLevel;    
    private String sourceAppName; //source appname
}