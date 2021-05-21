package com.tmax.WaplMath.Recommend.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_DEMO")
public class ProblemDemo {
	@Id
	private String probUuid;
	
	private String ukUuid;
	
	private String chapter;
	private String difficulty;
	private String probTypeUuid;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="chapter", insertable = false, updatable = false)
	private Curriculum curriculumDao;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="ukUuid", insertable = false, updatable = false)
	private Uk ukDao;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="typeId", insertable = false, updatable = false)
	private ProblemType problemType;
}
