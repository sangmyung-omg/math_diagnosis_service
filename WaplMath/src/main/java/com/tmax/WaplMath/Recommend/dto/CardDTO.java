package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Data;

/**
 * Learning schedule card DTO
 * @author Sangheon Lee
 */
@Data
public class CardDTO {
	private String cardType;
	private String cardTitle;
	private String firstProbLevel;
	private Integer estimatedTime;
	private Float cardScore;
	private String cardDetail;
	private List<ProblemSetDTO> probIdSetList;
}
