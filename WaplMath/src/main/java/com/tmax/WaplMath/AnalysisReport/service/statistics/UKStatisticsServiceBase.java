package com.tmax.WaplMath.AnalysisReport.service.statistics;

import java.util.List;

public interface UKStatisticsServiceBase {
    /**
     * Update all uk related statistics of all users
     */
    public int updateAllStatistics();


    public Float getMean(List<Float> masteryList);
    public List<Float> getSorted(List<Float> masteryList);
    public Float getSTD(List<Float> masteryList);
    public Float getPercentile(Float score, List<Float> sortedMastery);
}
