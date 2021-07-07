package com.tmax.WaplMath.Recommend.util;

import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public enum RecommendErrorCode implements ErrorCodeBase {

    GENERIC_ERROR("0001", "Generic error has occurred. "),
    DATA_MISMATCH_ERROR("0002", "Data mismatch error. "),

    TRITON_INFERENCE_ERROR("0003","Triton server returned error during inference. "),
    TRITON_JSON_PARSE_ERROR("0004","Triton server received invalid request JSON. "),
    TRITON_UNSUPPORTED_UK("0005","Triton server encountered unsupported UK. ");


    static final String ERROR_CODE_PREFIX = "ERR-REC-";

    private @Getter String errorCode;
    private @Getter String message;
    private @Getter HttpStatus status;

    private RecommendErrorCode(String errorCode, String message){
        this.errorCode = ERROR_CODE_PREFIX + errorCode;
        this.message = message;

        //If not designated, the status will be 500 (internal server error)
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private RecommendErrorCode(String errorCode, String message, HttpStatus status){
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
    }
}
