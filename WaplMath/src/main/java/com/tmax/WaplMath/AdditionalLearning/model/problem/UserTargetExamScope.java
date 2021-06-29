package com.tmax.WaplMath.AdditionalLearning.model.problem;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UserTargetExamScope {
	
	@Id
	private String userUuid;
	private String startSubSection;
	private String endSubSection;
}
