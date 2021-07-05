package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;

import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardConfigDTO {
	public String cardType;
	public Integer typeId; // for type card
	public List<TypeMasteryDTO> typeMasteryList; // for supple/addtlSupple card
	public String curriculumId; // for midExam/exam card
	public String midExamType; // "section", "chapter"
	public Integer probNum; // for exam type1/2 card
	public String examKeyword; // "3-2-final"
}
