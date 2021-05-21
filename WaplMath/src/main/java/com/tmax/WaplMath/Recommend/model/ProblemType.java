package com.tmax.WaplMath.Recommend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_TYPE_MASTER")
public class ProblemType {
	@Id
	private Integer typeId;
	
	private String typeName;
	private Integer sequence;
	
	@Column(name="CURRICULUM_ID")
	private String curriculumId;
}
