package com.tmax.WaplMath.DBUpdate.dto.problem;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedProbInfoResponseBaseDTO {
	private String probID;
	private String typeID;
	private String curriculumID;

	private String ansType;
	private String learningDomain;
	private String question;
	private String solution;
	private String source;
	private Float rate;
	private String difficulty;
	private String status;
	private String img;
	private Float timeRecommendation;
	private String frequent;
}

