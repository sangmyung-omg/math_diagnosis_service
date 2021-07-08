package com.tmax.WaplMath.Recommend.dto.lrs;

import lombok.Data;

@Data
public class LRSStatementResultDTO {
    private String userId;
    private String actionType;
    private String sourceType;
    private String sourceId;
    private String timestamp;
    private String platform;
    private Integer cursorTime;
    private String  userAnswer;
    private String duration;
    private String extension;
    private Integer isCorrect;
}
