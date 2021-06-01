package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ExamScheduleCardDTO {
	public String message;
	public String isCompletable;
	public List<CardDTO> cardList;
}
