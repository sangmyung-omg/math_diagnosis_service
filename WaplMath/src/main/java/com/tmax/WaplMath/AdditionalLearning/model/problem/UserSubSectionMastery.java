package com.tmax.WaplMath.AdditionalLearning.model.problem;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UserSubSectionMastery {
	
	@Id
	private String curriculumId;
	private double ukMastery;
}
