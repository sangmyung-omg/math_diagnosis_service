package com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;

public interface WaplScoreServiceBaseV0 {
    public WAPLScoreDTO getWaplScore(String userID);
    public WAPLScoreDTO getCurriculumWaplScore(String userID, String currID);

    /**
     * clear wapl score statistics of given user
     * @param userID
     * @return
     */
    public int clearWaplScoreStatistics(String userID);


    public static final String STAT_WAPL_SCORE = "waplscore";
    public static final String STAT_WAPL_SCORE_MASTERY = "waplscore_mastery";    
    public static final String STAT_WAPL_SCORE_MASTERY_TYPE_BASED = "waplscore_mastery_typebase";
}
