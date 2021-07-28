package com.tmax.WaplMath.Common.util.error;



import org.springframework.http.HttpStatus;

import lombok.Getter;

public enum CommonErrorCode implements ErrorCodeBase {
    GENERIC_ERROR("0001", "Generic error has occurred."),
    USER_NOT_EXIST("0002", "Given user does not exist. Please check if user is properly submitted through /userbasicinfo"), 

    LRS_STATEMENT_EMPTY("0003", "No LRS Statement found. Please check if user's info is properly submitted to LRS."),

    JWT_FIELD_NOT_FOUND("0004", "Requested field not found in jwt token."),
    JWT_INVALID("0005", "JWT has invalid format."),

    INVALID_ARGUMENT("0006", "Given argument is invalid"),

    //The lower bound error code;
    UNKNOWN_ERROR("9999", "Unknown error");


    static final String ERROR_CODE_PREFIX = "ERR-COM-";

    private @Getter String errorCode;
    private @Getter String message;
    private @Getter HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    private CommonErrorCode(String errorCode, String message){
        this.errorCode = ERROR_CODE_PREFIX + errorCode;
        this.message = message;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private CommonErrorCode(String errorCode, String message, HttpStatus status){
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }
}

