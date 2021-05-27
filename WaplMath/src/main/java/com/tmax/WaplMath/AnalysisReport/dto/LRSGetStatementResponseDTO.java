package com.tmax.WaplMath.AnalysisReport.dto;

import lombok.Data;

@Data
public class LRSGetStatementResponseDTO {
    private String userId;
    private String verbDisplay;
    private String sourceType;
    private String sourceId;
    private String timestamp;
    private String platform;
    private int cursorTime;
    private String userAnswer;
    private String duration;
    private String extension;
    private int isCorrect;
}
