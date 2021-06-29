package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;

import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardConfigDTO {
	public String cardType;
	public Integer typeId;
	public List<TypeMasteryDTO> lowMasteryTypeList;
	public String midExamCurriculumId;
	public String midExamType;
	public String trialExamType;
}
