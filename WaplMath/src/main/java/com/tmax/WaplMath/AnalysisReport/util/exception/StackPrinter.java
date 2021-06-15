package com.tmax.WaplMath.AnalysisReport.util.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackPrinter {
    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);
        return sw.toString();
    }
}
