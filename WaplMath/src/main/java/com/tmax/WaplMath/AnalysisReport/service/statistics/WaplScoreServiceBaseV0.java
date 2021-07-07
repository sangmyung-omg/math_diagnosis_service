package com.tmax.WaplMath.AnalysisReport.service.statistics;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;

public interface WaplScoreServiceBaseV0 {
    public WAPLScoreDTO getWaplScore(String userID);
    public WAPLScoreDTO getCurriculumWaplScore(String userID, String currID);


    public final String STAT_WAPL_SCORE = "waplscore";
    public final String STAT_WAPL_SCORE_MASTERY = "waplscore_mastery";
}
