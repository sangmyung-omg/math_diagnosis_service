package com.tmax.WaplMath.AnalysisReport.service.statistics.user;

import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

public interface UserStatisticsServiceBase {
    /**
     * method to update designated user's statistics
     * @param userID
     */
    public void updateSpecificUser(String userID);

    public void updateAllUsers();

    public int updateCustomUserStat(String userID, String statName, Statistics.Type dataType, String data);

    public Statistics getUserStatistics(String userID, String statName);



    public static final String STAT_TOTAL_MASTERY_MEAN = "total_mastery_mean";
    public static final String STAT_TOTAL_MASTERY_STD = "total_mastery_std";
    
    public static final String STAT_CURRICULUM_MASTERY_MAP = "curr_mastery";

    public static final String STAT_EXAMSCOPE_SCORE = "examscope_score";
}
