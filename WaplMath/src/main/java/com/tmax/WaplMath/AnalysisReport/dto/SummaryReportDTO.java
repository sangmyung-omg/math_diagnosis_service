package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryReportDTO {
    private double score;
    private double percentile;
    private double targetpercentile;
    private double average;
    private double std;
    private String commentary;
}