package com.tmax.WaplMath.AnalysisReport.dto.report;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPartMastery {
    private Map<String, Float> score;
    private Map<String, Float> waplScore;
}
