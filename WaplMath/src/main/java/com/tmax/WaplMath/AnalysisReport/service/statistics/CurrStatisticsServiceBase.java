package com.tmax.WaplMath.AnalysisReport.service.statistics;

public interface CurrStatisticsServiceBase {
    public void updateStatistics();

    public Statistics getStatistics(String currId, String statName);
}
