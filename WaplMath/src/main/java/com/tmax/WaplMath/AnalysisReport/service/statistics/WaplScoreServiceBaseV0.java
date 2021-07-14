package com.tmax.WaplMath.AnalysisReport.service.statistics;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;

public interface WaplScoreServiceBaseV0 {
    public WAPLScoreDTO getWaplScore(String userID);
    public WAPLScoreDTO getCurriculumWaplScore(String userID, String currID);

}
