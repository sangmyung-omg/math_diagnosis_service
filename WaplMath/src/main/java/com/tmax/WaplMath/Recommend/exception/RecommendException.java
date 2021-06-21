package com.tmax.WaplMath.Recommend.exception;


import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

public class RecommendException extends ResponseStatusException{
    @Getter
    private String errorCode;

    @Getter
    private String message;

    public RecommendException(String errorCode, String message){
        super(HttpStatus.INTERNAL_SERVER_ERROR);

        this.errorCode = errorCode;
        this.message = message;        
    }

    public RecommendException(RecommendErrorCode error){
        super(HttpStatus.INTERNAL_SERVER_ERROR);

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage();        
    }

    public RecommendException(RecommendErrorCode error, String appendMessage){
        super(HttpStatus.INTERNAL_SERVER_ERROR);

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage() + appendMessage;        
    }
}
