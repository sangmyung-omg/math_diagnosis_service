package com.tmax.WaplMath.AnalysisReport.service.statistics.score;

import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;

public interface ScoreServiceBase {
    public PersonalScoreDTO getUserScore(String userID, Set<String> excludeList);
    public PersonalScoreDTO getWaplScore(String userID, Set<String> excludeList);
    public PersonalScoreDTO getTargetScore(String userID, Set<String> excludeList);

    public GlobalStatisticDTO getScoreStats(String userID, Set<String> excludeList, int histogramSize);
}
