package com.tmax.WaplMath.AnalysisReport.model.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsAnalyticsTypeKey implements Serializable {
    private Integer typeId;
    private String name;
}
