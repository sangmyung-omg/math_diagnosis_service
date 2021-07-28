package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class JWTFieldNotFound extends GenericInternalException{
    public JWTFieldNotFound(String fieldName){
        super(CommonErrorCode.JWT_FIELD_NOT_FOUND, fieldName);
    }
}
