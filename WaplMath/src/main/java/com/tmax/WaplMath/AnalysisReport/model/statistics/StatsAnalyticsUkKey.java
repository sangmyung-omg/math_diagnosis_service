package com.tmax.WaplMath.AnalysisReport.model.statistics;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsAnalyticsUkKey implements Serializable{
    private Integer ukId;
    private String name;
}
