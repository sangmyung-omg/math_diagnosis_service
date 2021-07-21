package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class UserNotFoundException extends GenericInternalException {
    public UserNotFoundException(){
        super(CommonErrorCode.USER_NOT_EXIST, "Please check if user is properly submitted through /userbasicinfo");
    }
}
