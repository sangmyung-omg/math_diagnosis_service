package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class LRSStatementEmptyException extends UserOrientedException {
    public LRSStatementEmptyException(String userID){
        super(CommonErrorCode.LRS_STATEMENT_EMPTY, userID, "", "LRSStatementEmptyException");
    }
}
