package com.tmax.WaplMath.AnalysisReport.util.error;

import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public enum ARErrorCode implements ErrorCodeBase {
    GENERIC_ERROR("0001", "Generic error has occurred."),
    DATA_MISMATCH_ERROR("0002", "Data mismatch error."),

    MASTERY_SERVICE_ERROR("0003", "Mastery service error."),

    RESULT_SERVICE_ERROR("0004", "Result service error."),

    JSON_PROCESSING_ERROR("0005", "Error while processing JSON data."),

    TRITON_400_ERROR("0006", "Triton server returned a 400 error."),
    TRITON_500_ERROR("0007", "Triton server returned a 500 error."),

    NUMBER_PARSE_ERROR("0008", "Error while parsing number."),

    //The lower bound error code;
    UNKNOWN_ERROR("ERR-AR-999", "Unknown error");


    static final String ERROR_CODE_PREFIX = "ERR-AR-";

    private @Getter String errorCode;
    private @Getter String message;
    private @Getter HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    private ARErrorCode(String errorCode, String message){
        this.errorCode = ERROR_CODE_PREFIX + errorCode;
        this.message = message;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ARErrorCode(String errorCode, String message, HttpStatus status){
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }
}
