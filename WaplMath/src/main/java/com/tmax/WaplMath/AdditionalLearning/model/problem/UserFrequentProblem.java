package com.tmax.WaplMath.AdditionalLearning.model.problem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UserFrequentProblem {

	//private String userUuid;
	
	@Id
	@Column(name="PROB_ID")
	private int problemId;
	
	private String curriculumId;
	
}
