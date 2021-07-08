package com.tmax.WaplMath.AdditionalLearning.model.problem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class UserSectionMastery {
	
	@Id
	@Column(name="sub_section")
	private String section;
	private float ukMastery;
	
}
