package com.tmax.WaplMath.AnalysisReport.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolveSpeedDTO {
    private Float satisfyRate;
    private Integer problemcount;
}
