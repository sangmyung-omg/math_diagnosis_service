package com.tmax.WaplMath.Common.exception;


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
}
