package com.tmax.WaplMath.AnalysisReport.dto.uk;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UkDataDTO {
    private Integer ukID;
    private String name;
    private GlobalStatisticDTO stats;
}
