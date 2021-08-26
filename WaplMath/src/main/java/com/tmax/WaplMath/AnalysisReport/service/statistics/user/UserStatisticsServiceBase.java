package com.tmax.WaplMath.AnalysisReport.service.statistics.user;

import java.util.Optional;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.model.statistics.StatsAnalyticsUser;
import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

public interface UserStatisticsServiceBase {
    /**
     * method to update designated user's statistics. deprecated. left for compat purpose
     * @deprecated
     * @param userID
     */
    @Deprecated public void updateSpecificUser(String userID);

    /**
     * method to update specific user
     * @param userID userID of user to update
     * @param updateDB option to commit to DB directly
     * @return Set of rows to update to stat user table
     */
    public Set<StatsAnalyticsUser> updateSpecificUser(String userID, boolean updateDB);

    /**
     * method to update all users in DB
     */
    public void updateAllUsers();
    
    /**
     * Method to update a specific stat of given user
     * @param userID User ID of target user
     * @param statName Stat name of the statistic
     * @param dataType data type of statistic
     * @param data data (String)
     * @return update status
     */
    public int updateCustomUserStat(String userID, String statName, Statistics.Type dataType, String data);

    /**
     * method to read user's given statistic
     * @param userID target user's ID
     * @param statName stat to read
     * @return Stat. null if not found
     */
    public Statistics getUserStatistics(String userID, String statName);

    /**
     * Wrapper of getUserStatistics with optional
     * @param userID target user's ID
     * @param statName stat to read
     * @return Stat. null if not found
     */
    public Optional<Statistics> getUserStatisticsOpt(String userID, String statName);

    /**
     * Method to check if stat exists
     * @param userID target user's ID
     * @param statName stat name to check existance
     * @return existance of stat for given user
     */
    public boolean hasUserStatistics(String userID, String statName);

    /**
     * method to remove set of stats of given user
     * @param userID target user
     * @param statNames Set of stat names to clear
     * @return rows deleted
     */
    public boolean clearUserStatistics(String userID, Iterable<String> statNames);


    //Constants for stat name
    public static final String STAT_TOTAL_MASTERY_MEAN = "total_mastery_mean";
    public static final String STAT_TOTAL_MASTERY_STD = "total_mastery_std";
    
    public static final String STAT_CURRICULUM_MASTERY_MAP = "curr_mastery";

    public static final String STAT_EXAMSCOPE_SCORE = "examscope_score";
    public static final String STAT_EXAMSCOPE_PERCENTILE_LUT = "examscope_percentile_lut";

    public static final String STAT_EXAMSCOPE_SCORE_HISTORY = "examscope_score_hist";

    public static final String STAT_CORRECT_RATE = "correct_rate";
    public static final String STAT_SOLVING_SPEED_SATISFY_RATE = "solve_speed_satisfy_rate";
    public static final String STAT_RATE_PROBLEM_COUNT = "rate_problem_count";

    public static final String STAT_CORRECT_CNT = "correct_cnt";
    public static final String STAT_PASS_CNT = "pass_cnt";
    public static final String STAT_WRONG_CNT = "wrong_cnt";

    public static final String STAT_WAPL_SCORE = "waplscore";
    public static final String STAT_WAPL_SCORE_MASTERY = "waplscore_mastery";    

    public static final String STAT_RECENT_CURR_ID_LIST = "recent_curr_list";
    public static final String STAT_RECENT_DIAGNOSIS_CURR_ID_LIST = "recent_diag_curr_list";

    public static final String STAT_LRS_STATEMENT_HISTORY = "lrs_statement_list";
}