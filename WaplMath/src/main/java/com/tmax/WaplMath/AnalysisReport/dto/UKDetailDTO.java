package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UKDetailDTO {
    private String id;
    private String name;
    private double skillScore;
}