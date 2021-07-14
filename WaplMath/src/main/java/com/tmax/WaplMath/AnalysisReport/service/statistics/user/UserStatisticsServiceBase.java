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

    public boolean hasUserStatistics(String userID, String statName);



    public static final String STAT_TOTAL_MASTERY_MEAN = "total_mastery_mean";
    public static final String STAT_TOTAL_MASTERY_STD = "total_mastery_std";
    
    public static final String STAT_CURRICULUM_MASTERY_MAP = "curr_mastery";

    public static final String STAT_EXAMSCOPE_SCORE = "examscope_score";
    public static final String STAT_EXAMSCOPE_PERCENTILE_LUT = "examscope_percentile_lut";

    public static final String STAT_CORRECT_RATE = "correct_rate";
    public static final String STAT_SOLVING_SPEED_SATISFY_RATE = "solve_speed_satisfy_rate";
    public static final String STAT_RATE_PROBLEM_COUNT = "rate_problem_cpunt";

    public static final String STAT_WAPL_SCORE = "waplscore";
    public static final String STAT_WAPL_SCORE_MASTERY = "waplscore_mastery";    

    public static final String STAT_RECENT_CURR_ID_LIST = "recent_curr_list";  
}