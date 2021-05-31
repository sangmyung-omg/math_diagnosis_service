package com.tmax.WaplMath.Recommend.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Problem.model.Problem;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_UK_REL")
public class ProblemUkRel {
	@Id
	private Integer probUkRelId;
	
	private Integer probId;
	private	Integer ukId;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="probId", insertable = false, updatable = false)
	private Problem problem;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="ukId", insertable = false, updatable = false)
	private Uk uk;
}
