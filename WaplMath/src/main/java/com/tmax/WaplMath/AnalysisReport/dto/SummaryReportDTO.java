package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryReportDTO {
    private double score;
    private double percentile;
}