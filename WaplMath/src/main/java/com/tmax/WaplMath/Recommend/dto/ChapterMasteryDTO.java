package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChapterMasteryDTO {
	private String part;
	private List<String> chapterList;
	private List<Integer> ukList;
	private List<Float> masteryList;
	
	private String chapterName;
	private Float chapterMastery;
}
