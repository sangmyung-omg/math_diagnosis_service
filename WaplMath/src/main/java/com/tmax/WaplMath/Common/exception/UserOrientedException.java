package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;

import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.*;

@Slf4j
public class UserOrientedException extends BaseException  {

    public UserOrientedException(ErrorCodeBase error, String userID){
        super(error.getStatus());

        log.error(this.getClass().getSimpleName(), 
                  kv("errorUser", userID), 
                  kv("errorCode", error.getErrorCode()),
                  kv("errorMsg", error.getMessage()));

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage();   
    }

    public UserOrientedException(ErrorCodeBase error, String userID, String appendMessage){
        super(error.getStatus());

        log.error(this.getClass().getSimpleName(), 
                  kv("errorUser", userID), 
                  kv("errorCode", error.getErrorCode()),
                  kv("errorMsg", error.getMessage() + appendMessage));

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage() + " [" + userID + "] " + appendMessage;   
    }


    public UserOrientedException(ErrorCodeBase error, String userID, String appendMessage, String exceptionName){
        super(error.getStatus());

        log.error(exceptionName, 
                  kv("errorUser", userID), 
                  kv("errorCode", error.getErrorCode()),
                  kv("errorMsg", error.getMessage() + appendMessage));

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage() + " [" + userID + "] " + appendMessage;   
    }
}
