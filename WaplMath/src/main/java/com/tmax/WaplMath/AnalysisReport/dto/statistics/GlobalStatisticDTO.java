package com.tmax.WaplMath.AnalysisReport.dto.statistics;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalStatisticDTO {
    private Float std;
    private Float mean;
    private Float median;
    private List<Integer> histogram;
    private Integer totalCnt;
}
