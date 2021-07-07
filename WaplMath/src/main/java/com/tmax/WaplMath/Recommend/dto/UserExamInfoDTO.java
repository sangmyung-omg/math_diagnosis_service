package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserExamInfoDTO {
	public String examType;
	public String examStartDate;
	public String examDueDate;
	public String startSubSectionId;
	public String endSubSectionId;
	public List<String> exceptSubSectionIdList;
}
