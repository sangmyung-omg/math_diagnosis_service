package com.tmax.WaplMath.Common.exception;

public class InvalidArgumentException extends GenericInternalException {
    public InvalidArgumentException(){
        super("ERR-0004", "Invalid parameter");
    }

    public InvalidArgumentException(String message){
        super("ERR-0004", "Invalid parameter: " + message);
    }
}
