package com.tmax.WaplMath.Recommend.model.problem;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "DIAGNOSIS_PROBLEM")
public class DiagnosisProblem {

	@Id
	private Integer diagnosisProbId;

	private Integer basicProbId;

	private Integer upperProbId;

	private Integer lowerProbId;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "basicProbId", insertable = false, updatable = false)
	private Problem basicProblem;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "upperProbId", insertable = false, updatable = false)
	private Problem upperProblem;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "lowerProbId", insertable = false, updatable = false)
	private Problem lowerProblem;
}
