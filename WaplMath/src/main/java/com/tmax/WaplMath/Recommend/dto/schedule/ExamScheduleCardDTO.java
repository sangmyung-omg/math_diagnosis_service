package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;

import lombok.Data;

@Data
public class ExamScheduleCardDTO {
	public String message; 
	public List<CardDTOV1> cardList;
}
