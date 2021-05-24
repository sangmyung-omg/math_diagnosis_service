package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.Data;

@Data
public class LevelDiagnosisRecordDTO {
    private int numCorrect;
    private int numWrong;
    private int numDontknow;
    private int timeConsumed;
}