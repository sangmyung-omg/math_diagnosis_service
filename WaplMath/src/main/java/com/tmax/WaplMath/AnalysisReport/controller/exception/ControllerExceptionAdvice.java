package com.tmax.WaplMath.AnalysisReport.controller.exception;

import com.tmax.WaplMath.Common.dto.GenericErrorDTO;
import com.tmax.WaplMath.Common.exception.BaseException;
import com.tmax.WaplMath.Common.util.exception.StackPrinter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Controller exception advice for Analysis report
 * @author Jonghyun Seong
 */
@RestControllerAdvice("com.tmax.WaplMath.AnalysisReport.controller")
public class ControllerExceptionAdvice {
    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleBaseExceptionError(BaseException exception){
        return new GenericErrorDTO(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleInternalError(IllegalArgumentException exception){
        return new GenericErrorDTO("ERR-0004", "Invalid parameter. " + StackPrinter.getStackTrace(exception));
    }
}
