package com.tmax.WaplMath.AnalysisReport.service.statistics.type;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsType;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

public interface TypeStatisticsServiceBase {
    /**
     * Update all type related statistics of all users
     */
    public int updateAllStatistics();
    public boolean updateStatistics(boolean isForced);

    public Statistics getTypeStatistics(Integer typeID, String statname);


    public Float getMean(List<Float> masteryList);
    public List<Float> getSorted(List<Float> masteryList);
    public Float getSTD(List<Float> masteryList);
    public Float getPercentile(Float score, List<Float> sortedMastery);

    public List<StatsAnalyticsType> getAllOfTypeIds(List<Integer> typeIds);

    //Public key values
    public static final String STAT_MASTERY_SORTED = "mastery_sorted";
    public static final String STAT_MASTERY_MEAN = "mastery_mean";
    public static final String STAT_MASTERY_MEDIAN = "mastery_median";
    public static final String STAT_MASTERY_STD = "mastery_std";
    public static final String STAT_MASTERY_PERCENTILE_LUT = "mastery_percentile_lut";
}
