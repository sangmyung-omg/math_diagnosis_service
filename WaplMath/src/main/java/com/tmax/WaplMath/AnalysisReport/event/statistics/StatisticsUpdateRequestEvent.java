package com.tmax.WaplMath.AnalysisReport.event.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsUpdateRequestEvent {
    private String userID;
}
