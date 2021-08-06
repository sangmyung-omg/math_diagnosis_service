package com.tmax.WaplMath.Common.util.kafka;

import com.tmax.WaplMath.Common.config.KafkaTopicConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.net.InetAddress;


import lombok.extern.slf4j.Slf4j;

@Component("KafkaPublisher")
@Slf4j
public class KafkaPublisher {
    @Autowired(required = false)
    private KafkaTopicConfig kafkaConfig;
    
    @Autowired(required = false)
    private KafkaTemplate<String, KafkaEvent> kafkaTemplate;

    @Value("${app.name}")
    private String sourceAppName; //source appname
    
    private String hostname = null; //Hostname to inject to event

    private static KafkaPublisher instance;

    public KafkaPublisher(){
        //Set hostname. maynot work in windows
        try{
            this.hostname = InetAddress.getLocalHost().getHostName();
        }
        catch(Throwable e){
            log.error("Cannot get hostname");
        }

        KafkaPublisher.instance = this;
    }

    public static KafkaPublisher getInstance(){
        //This will always be not null as long as spring injections do not fail
        return instance;
    }

    public void publishMessage(String userID, KafkaEventType eventType, String eventCode, String eventMsg, KafkaEventLevel eventLevel){
        publishMessage(KafkaEvent.builder()
                                .eventType(eventType)
                                .eventCode(eventCode)
                                .eventMsg(eventMsg)
                                .eventLevel(eventLevel)
                                .sourceUser(userID)
                                .build()
                                );
    }

    public void publishMessage(KafkaEvent event){
        //This case, the useKafka option is not enabled
        if(kafkaConfig == null) {
            return;
        }

        //Inject hostname, appname
        event.setSourceHost(hostname);
        event.setSourceAppName(sourceAppName);

        //Build message
        Message<KafkaEvent> message = MessageBuilder.withPayload(event)
                                                .setHeader(KafkaHeaders.TOPIC, kafkaConfig.getTopicName())
                                                .build();


        //Send message to kafka and get a async return obj.
        ListenableFuture<SendResult<String, KafkaEvent>> future = kafkaTemplate.send(message);

        //Add listeners with loggers to the future object.
        future.addCallback(new ListenableFutureCallback<SendResult<String, KafkaEvent>>(){
            @Override
            public void onSuccess(SendResult<String, KafkaEvent> result) {
                // log.info("Sent message [{}] offset [{}]", event.toString(), result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Failed to send message [{}] error [{}]", event.toString(), ex.getMessage());
            }
        });
    }
}
