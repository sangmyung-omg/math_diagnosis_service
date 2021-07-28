package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class JWTInvalidException extends GenericInternalException {
    public JWTInvalidException(){
        super(CommonErrorCode.JWT_INVALID);
    }

    public JWTInvalidException(String message){
        super(CommonErrorCode.JWT_INVALID, message);
    }
}
