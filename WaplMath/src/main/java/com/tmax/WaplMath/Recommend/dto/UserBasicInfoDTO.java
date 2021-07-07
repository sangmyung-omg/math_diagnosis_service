package com.tmax.WaplMath.Recommend.dto;

import lombok.Data;

@Data
public class UserBasicInfoDTO {
	private String grade;
	private String semester;
	private String name;
	private String currentCurriculumId;
	private Integer targetScore;
}
