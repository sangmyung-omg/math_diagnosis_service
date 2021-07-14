package com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum;

import java.util.List;
import java.util.Map;

import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

public interface CurrStatisticsServiceBase {
    /**
     * Update curriculum based statistics
     */
    public void updateStatistics();
    public boolean updateStatistics(boolean isForced);

    /**
     * Returns statistics of given curriculum ID and statistic name
     * @param currId String curriculum ID
     * @param statName String statistic name
     * @return Statistics data of the given parameters
     */
    public Statistics getStatistics(String currId, String statName);

    public Statistics getCoarseAverageStatistics(List<String> currIdList, String statName);


    /**
     * Returns Map of <Curriculum ID, Mastery (float)> pair. Calculates mastery for the given userID
     * @param userID
     * @return
     */
    public Map<String, Float> getCurriculumMasteryOfUser(String userID);

    public void setStatistics(String currID, String statname, Statistics stats);


    //Public key values
    public static final String STAT_MASTERY_SORTED = "mastery_sorted";
    public static final String STAT_MASTERY_MEAN = "mastery_mean";
    public static final String STAT_MASTERY_MEDIAN = "mastery_median";
    public static final String STAT_MASTERY_STD = "mastery_std";
    public static final String STAT_MASTERY_PERCENTILE_LUT = "mastery_percentile_lut";

    public static final String STAT_MASTERY_HISTOGRAM = "mastery_histogram";
}
