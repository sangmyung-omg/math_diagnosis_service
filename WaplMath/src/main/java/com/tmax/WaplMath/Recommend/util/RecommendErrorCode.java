package com.tmax.WaplMath.Recommend.util;

import lombok.Getter;

public enum RecommendErrorCode {

    GENERIC_ERROR("ERR-REC-001", "Generic error has occurred. "),
    DATA_MISMATCH_ERROR("ERR-REC-002", "Data mismatch error. "),
    TRITON_INFERENCE_ERROR("ERR-REC-003","Triton server returned error during inference. ");


    private @Getter String errorCode;
    private @Getter String message;

    private RecommendErrorCode(String errorCode, String message){
        this.errorCode = errorCode;
        this.message = message;
    }
}
