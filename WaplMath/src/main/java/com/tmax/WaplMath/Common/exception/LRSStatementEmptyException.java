package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class LRSStatementEmptyException extends GenericInternalException {
    public LRSStatementEmptyException(){
       super(CommonErrorCode.LRS_STATEMENT_EMPTY); 
    }
}
