package com.tmax.WaplMath.AdditionalLearning.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tmax.WaplMath.Common.dto.GenericErrorDTO;
import com.tmax.WaplMath.Common.exception.GenericInternalException;

@RestControllerAdvice("com.tmax.WaplMath.AdditionalLearning.controller")
public class AddLearnControllerAdvice {
	@ExceptionHandler(GenericInternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericErrorDTO handleInternalError(GenericInternalException exception){
        return new GenericErrorDTO(exception.getErrorCode(), exception.getMessage());
    }
}
