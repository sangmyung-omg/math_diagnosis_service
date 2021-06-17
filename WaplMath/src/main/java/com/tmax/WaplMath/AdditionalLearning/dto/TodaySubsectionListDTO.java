package com.tmax.WaplMath.AdditionalLearning.dto;

import java.util.List;

import lombok.Data;


@Data
public class TodaySubsectionListDTO {
	
	//진단고사 직후 인지 아닌지
	private Boolean isFirstFrequent;
	//curriculum_id
	private List<String> todaySubsectionList;
}
