package com.tmax.WaplMath.AdditionalLearning.dto;

import java.util.List;

import lombok.Data;

@Data
public class FrequentCardDTO {
	
	private String resultMessage;
	
	private String cardType;
	private Integer estimatedTime;
	private List<FrequentProblemDTO> probSetList;
	private List<SubsectionMasteryDTO> subsectionSetList;
	
}
