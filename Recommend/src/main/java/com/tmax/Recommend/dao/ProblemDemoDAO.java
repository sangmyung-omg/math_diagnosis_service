package com.tmax.Recommend.dao;

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
public class ProblemDemoDAO {
	@Id
	private String probUuid;
	
	private String ukUuid;
	
	private String chapter;
	private String difficulty;
	private String probTypeUuid;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="chapter", insertable = false, updatable = false)
	private CurriculumDAO curriculumDao;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="ukUuid", insertable = false, updatable = false)
	private UkDAO ukDao;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="probTypeUuid", insertable = false, updatable = false)
	private TypeUkDAO typeUkDao;
}
