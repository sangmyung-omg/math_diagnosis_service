package com.tmax.WaplMath.Recommend.dto;

import lombok.Data;

@Data
public class UserExamInfoDTO {
	public String examType;
	public String examStartDate;
	public String examDueDate;
	public Integer targetScore;
}
