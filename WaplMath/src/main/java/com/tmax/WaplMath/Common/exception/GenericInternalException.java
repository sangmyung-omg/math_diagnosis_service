package com.tmax.WaplMath.Common.exception;


import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

public class GenericInternalException extends ResponseStatusException{
    @Getter
    private String errorCode;

    @Getter
    private String message;

    public GenericInternalException(String errorCode, String message){
        super(HttpStatus.INTERNAL_SERVER_ERROR);

        this.errorCode = errorCode;
        this.message = message;        
    }

    public GenericInternalException(ErrorCodeBase error){
        super(error.getStatus());

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage();   
    }

    public GenericInternalException(ErrorCodeBase error, String appendMessage){
        super(error.getStatus());

        this.errorCode = error.getErrorCode();
        this.message = error.getMessage() + " | " + appendMessage;   
    }
}
