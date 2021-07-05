package com.tmax.WaplMath.Recommend.dto;

import lombok.Data;

/**
 * LRS StatementList POST api output
 * @author Sangheon_lee
 * @since 2021-07-05
 */
@Data
public class StatementDTO {
	public String actionType;
	public Integer cursorTime;
	public String duration;
	public String extension;
	public Integer isCorrect;
	public String platform;
	public String sourceId;
	public String sourceType;
	public String timestamp;
	public String userAnswer;
	public String userId;
}
