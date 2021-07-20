package com.tmax.WaplMath.AdditionalLearning.util.error;

import org.springframework.http.HttpStatus;

import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;

import lombok.Getter;

public enum AddLearnErrorCode implements ErrorCodeBase {
	GENERIC_ERROR("0001", "Generic error has occurred."),
	INVALID_PARAMETER("0002", "Invalid parameter."),
	
	DATA_NOT_FOUND("0003", "Data not found.");
	
	
	static final String ERROR_CODE_PREFIX = "ERR-AL-";

    private @Getter String errorCode;
    private @Getter String message;
    private @Getter HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    private AddLearnErrorCode(String errorCode, String message){
        this.errorCode = ERROR_CODE_PREFIX + errorCode;
        this.message = message;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private AddLearnErrorCode(String errorCode, String message, HttpStatus status){
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }

}
