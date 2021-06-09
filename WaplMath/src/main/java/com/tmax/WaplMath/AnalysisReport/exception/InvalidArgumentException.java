package com.tmax.WaplMath.AnalysisReport.exception;

public class InvalidArgumentException extends GenericInternalException {
    public InvalidArgumentException(){
        super("ERR-0004", "Invalid parameter");
    }

    public InvalidArgumentException(String message){
        super("ERR-0004", "Invalid parameter: " + message);
    }
}
