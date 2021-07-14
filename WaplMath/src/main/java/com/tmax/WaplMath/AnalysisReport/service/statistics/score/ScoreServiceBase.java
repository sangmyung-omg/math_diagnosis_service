package com.tmax.WaplMath.AnalysisReport.service.statistics.score;

import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.CorrectRateDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.SolveSpeedDTO;

public interface ScoreServiceBase {
    public PersonalScoreDTO getUserScore(String userID, Set<String> excludeSet);
    public PersonalScoreDTO getWaplScore(String userID, Set<String> excludeSet);
    public PersonalScoreDTO getTargetScore(String userID, Set<String> excludeSet);

    public GlobalStatisticDTO getScoreStats(String userID, Set<String> excludeSet, int histogramSize);


    //TEMP

    public CorrectRateDTO getCorrectRate(String userID, Set<String> excludeSet);
    public SolveSpeedDTO getSolveSpeedRate(String userID, Set<String> excludeSet);
}
