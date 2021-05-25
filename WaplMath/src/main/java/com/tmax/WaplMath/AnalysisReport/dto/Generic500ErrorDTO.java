package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.Data;

@Data
public class Generic500ErrorDTO {
    private String errorCode = "ERR-1001";
    private String message = "Invalid Token";
}
