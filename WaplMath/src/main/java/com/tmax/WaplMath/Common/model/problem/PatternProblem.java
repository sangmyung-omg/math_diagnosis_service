package com.tmax.WaplMath.Common.model.problem;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PATTERN_PROBLEM")
public class PatternProblem {
	@Id
	private Integer patternProbId;
	
	private Integer basicProbId;
	private Integer secondProbId;
	private Integer thirdProbId;
	
	@ManyToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="basicProbId", referencedColumnName="probId", insertable=false, updatable=false)
	private Problem basicProblem;
	
	@ManyToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="secondProbId", referencedColumnName="probId", insertable=false, updatable=false)
	private Problem secondProblem;
	
	@ManyToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="thirdProbId", referencedColumnName="probId", insertable=false, updatable=false)
	private Problem thirdProblem;
}
