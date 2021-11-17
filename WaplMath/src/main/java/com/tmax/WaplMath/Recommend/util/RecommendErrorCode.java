package com.tmax.WaplMath.Recommend.util;

import com.tmax.WaplMath.Common.util.error.ErrorCodeBase;

import org.springframework.http.HttpStatus;

import lombok.Getter;

public enum RecommendErrorCode implements ErrorCodeBase {

    GENERIC_ERROR("0001", "Generic error has occurred. "),
    DATA_MISMATCH_ERROR("0002", "Data mismatch error. "),

    TRITON_INFERENCE_ERROR("0003","Triton server returned error during inference. "),
    TRITON_JSON_PARSE_ERROR("0004","Triton server received invalid request JSON. "),
    TRITON_UNSUPPORTED_UK("0005","Triton server encountered unsupported UK. "),

    USER_NOT_EXIST_ERROR("0006", "Given user does not exist. Call /userbasicinfo PUT service first. "),
    USER_INFO_NULL_ERROR("0007", "One of user's info is null. "),
    USER_MASTERY_NOT_EXIST_ERROR("0008", "User's mastery is null. Call /mastery PUT service for initialize USER_KNOWLEDGE TB. "),

    NUMBER_PARSE_ERROR("0009", "Error while parsing number. "),

    LRS_NO_STATEMENT("0010", "No statement from LRS. "),

    SCHEDULE_CONFIGURATOR_ERROR("1000","Schedule configurator returned error. "),
    CARD_GENERATOR_ERROR("1002","Card generator returned error. "),    
    NO_PROBS_ERROR("1003","No problems to generate card. Probably in 2nd semester range. Error card type = "),
    EXAM_PASSED_ERROR("1004","Exam date has passed. Call /userexaminfo PUT service to reset the exam start/due date. Exam due date = "),

    CARD_GENERATE_NO_CARDS_ERROR("7777","No cards were created. User seems to have solved all the problems. ");

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
