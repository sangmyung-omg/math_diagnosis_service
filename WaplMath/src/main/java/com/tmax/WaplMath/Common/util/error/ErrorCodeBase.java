package com.tmax.WaplMath.Common.util.error;

import org.springframework.http.HttpStatus;

public interface ErrorCodeBase {
    public HttpStatus getStatus();
    public String getErrorCode();
    public String getMessage();
}
