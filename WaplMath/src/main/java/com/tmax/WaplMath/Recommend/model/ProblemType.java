package com.tmax.WaplMath.Recommend.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
	
	private String curriculumId;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="curriculumId", insertable = false, updatable = false)
	private Curriculum curriculum;
}
