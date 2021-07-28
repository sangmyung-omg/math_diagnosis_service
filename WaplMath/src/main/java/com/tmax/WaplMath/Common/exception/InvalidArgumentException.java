package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class InvalidArgumentException extends GenericInternalException {
    public InvalidArgumentException(){
        super(CommonErrorCode.INVALID_ARGUMENT);
    }

    public InvalidArgumentException(String message){
        super(CommonErrorCode.INVALID_ARGUMENT, message);
    }
}
