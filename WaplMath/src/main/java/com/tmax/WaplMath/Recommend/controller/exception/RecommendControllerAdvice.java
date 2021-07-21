package com.tmax.WaplMath.Recommend.controller.exception;

import com.tmax.WaplMath.Common.dto.GenericErrorDTO;
import com.tmax.WaplMath.Common.util.exception.StackPrinter;
import com.tmax.WaplMath.Recommend.exception.RecommendException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice("com.tmax.WaplMath.Recommend.controller")
public class RecommendControllerAdvice {
    @ExceptionHandler(RecommendException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleInternalError(RecommendException exception){
        log.error("Return [{}] {}, {}", exception.getErrorCode(), exception.getMessage());
        return new GenericErrorDTO(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleInternalError(IllegalArgumentException exception){
        log.error("Return [ERR-0004] {}", "Invalid parameter:" + StackPrinter.getStackTrace(exception));
        return new GenericErrorDTO("ERR-0004", "Invalid parameter:" + StackPrinter.getStackTrace(exception));
    }
}
