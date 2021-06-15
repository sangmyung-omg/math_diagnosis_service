package com.tmax.WaplMath.Common.exception;

public class InvalidTokenException extends GenericInternalException{
    public InvalidTokenException(){
        super("ERR-0001", "Invalid Token");
    }

    public InvalidTokenException(String message){
        super("ERR-0001", "Invalid Token: " + message);
    }
}
