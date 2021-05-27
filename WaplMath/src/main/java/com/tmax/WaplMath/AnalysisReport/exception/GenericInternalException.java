package com.tmax.WaplMath.AnalysisReport.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GenericInternalException extends ResponseStatusException{
    public GenericInternalException(String message){
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @Override
    public HttpHeaders getResponseHeaders() {
        // TODO Auto-generated method stub
        return super.getResponseHeaders();
    }
}
