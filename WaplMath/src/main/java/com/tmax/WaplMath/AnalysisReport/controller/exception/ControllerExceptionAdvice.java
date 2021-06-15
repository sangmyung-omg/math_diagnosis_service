package com.tmax.WaplMath.AnalysisReport.controller.exception;

import com.tmax.WaplMath.AnalysisReport.dto.GenericErrorDTO;
import com.tmax.WaplMath.Common.exception.GenericInternalException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("com.tmax.WaplMath.AnalysisReport.controller")
public class ControllerExceptionAdvice {
    @ExceptionHandler(GenericInternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleInternalError(GenericInternalException exception){
        return new GenericErrorDTO(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleInternalError(IllegalArgumentException exception){
        return new GenericErrorDTO("ERR-0004", "Invalid parameter");
    }
}
