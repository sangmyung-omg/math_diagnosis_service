package com.tmax.WaplMath.Common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Data;

@Data
public class BaseException extends ResponseStatusException{
    protected String errorCode;
    protected String message;

    public BaseException(HttpStatus status) {
        super(status);
    }

    public BaseException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
