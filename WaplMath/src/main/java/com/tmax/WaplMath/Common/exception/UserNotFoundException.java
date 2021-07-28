package com.tmax.WaplMath.Common.exception;

import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

public class UserNotFoundException extends UserOrientedException {
    public UserNotFoundException(){
        super(CommonErrorCode.USER_NOT_EXIST, "unidentified", "", "UserNotFoundException");
    }

    public UserNotFoundException(String userID){
        super(CommonErrorCode.USER_NOT_EXIST, userID, "", "UserNotFoundException");
    }
}
