package com.tmax.Recommend.model;

import java.util.List;

import lombok.Data;

@Data
public class ChapterMasteryDTO {
	private String part;
	private List<String> chapterList;
	private List<String> ukList;
	private List<Float> masteryList;
	
	private String chapterName;
	private Float chapterMastery;
}
