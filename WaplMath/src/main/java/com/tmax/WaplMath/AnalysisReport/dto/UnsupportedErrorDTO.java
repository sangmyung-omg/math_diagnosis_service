package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsupportedErrorDTO {
    private String errorCode = "ERR-1002";
    private String message = "Unsupported API (곧 지원 예정)";
}
