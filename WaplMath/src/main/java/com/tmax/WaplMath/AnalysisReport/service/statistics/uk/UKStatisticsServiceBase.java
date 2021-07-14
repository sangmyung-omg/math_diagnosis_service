package com.tmax.WaplMath.AnalysisReport.service.statistics.uk;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

public interface UKStatisticsServiceBase {
    /**
     * Update all uk related statistics of all users
     */
    public int updateAllStatistics();

    public Statistics getUKStatistics(Integer ukID, String statname);


    public Float getMean(List<Float> masteryList);
    public List<Float> getSorted(List<Float> masteryList);
    public Float getSTD(List<Float> masteryList);
    public Float getPercentile(Float score, List<Float> sortedMastery);

    //Public key values
    public static final String STAT_MASTERY_SORTED = "mastery_sorted";
    public static final String STAT_MASTERY_MEAN = "mastery_mean";
    public static final String STAT_MASTERY_MEDIAN = "mastery_median";
    public static final String STAT_MASTERY_STD = "mastery_std";
    public static final String STAT_MASTERY_PERCENTILE_LUT = "mastery_percentile_lut";
}
