package com.tmax.WaplMath.AnalysisReport.service.diagnosis;

import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;

public interface DiagnosisServiceBase {
    /**
     * Method to get diagnosis score of given user
     * @param userID
     * @return
     */
    public PersonalScoreDTO getScore(String userID);


    public static final String STAT_DIAGNOSIS_SCORE = "diagnosis_score";
}
