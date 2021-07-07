package com.tmax.WaplMath.AnalysisReport.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WAPLScoreDTO {
    private float score;
    private float percentile;

    static public WAPLScoreDTO getScaledScore(WAPLScoreDTO in){
        return WAPLScoreDTO.builder()
                           .score(100*in.score)
                           .percentile(100*in.percentile)
                           .build();
    }
}
