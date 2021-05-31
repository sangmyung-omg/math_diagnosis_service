package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelDiagnosisRecordDTO {
    private int numCorrect;
    private int numWrong;
    private int numDontknow;
    private int timeConsumed;
}