package com.tmax.WaplMath.Common.exception;


import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;
import com.tmax.WaplMath.Common.util.kafka.*;

import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.*;

@Slf4j
public class GenericInternalException extends BaseException {

    public GenericInternalException(String errorCode, String message){
        super(HttpStatus.INTERNAL_SERVER_ERROR);

        this.errorCode = errorCode;
        this.message = message;        
    }

    public GenericInternalException(ErrorCodeBase error){
        this(error, "", "");
        // super(error.getStatus());

        // log.error(this.getClass().getSimpleName() + " {} {}", 
        //         kv("errorCode", error.getErrorCode()),
        //         kv("errorMsg", error.getMessage()));

        // this.errorCode = error.getErrorCode();
        // this.message = error.getMessage();   
    }

    public GenericInternalException(ErrorCodeBase error, String appendMessage){
        this(error, appendMessage, "");
        // super(error.getStatus());

        // log.error(this.getClass().getSimpleName() + " {} {}", 
        //         kv("errorCode", error.getErrorCode()),
        //         kv("errorMsg", error.getMessage() + appendMessage));

        // this.errorCode = error.getErrorCode();
        // this.message = error.getMessage() + " | " + appendMessage;   
    }

    public GenericInternalException(ErrorCodeBase error, String appendMessage, String exceptionName){
        super(error.getStatus());

        log.error(exceptionName  + " {} {}", 
                  kv("errorCode", error.getErrorCode()),
                  kv("errorMsg", error.getMessage() + appendMessage));

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage() + " | " + appendMessage;   

        //Publish kafka event
        KafkaPublisher.getInstance().publishMessage(KafkaEvent.builder()
                                                                .eventType(KafkaEventType.EXCEPTION)
                                                                .eventCode(error.getErrorCode())
                                                                .eventLevel(KafkaEventLevel.ERROR)
                                                                .eventMsg(error.getMessage() + appendMessage + exceptionName)
                                                                .build()
                                                                );
    }
}
