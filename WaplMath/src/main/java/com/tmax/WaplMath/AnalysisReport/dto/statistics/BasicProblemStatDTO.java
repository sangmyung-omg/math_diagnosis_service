package com.tmax.WaplMath.AnalysisReport.dto.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicProblemStatDTO {
    private Integer correct;
    private Integer pass;
    private Integer wrong;
    private Integer totalcnt;
}
