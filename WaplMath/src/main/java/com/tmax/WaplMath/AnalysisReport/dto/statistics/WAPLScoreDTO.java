package com.tmax.WaplMath.AnalysisReport.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WAPLScoreDTO {
    private float score;
    private float percentile;
}
