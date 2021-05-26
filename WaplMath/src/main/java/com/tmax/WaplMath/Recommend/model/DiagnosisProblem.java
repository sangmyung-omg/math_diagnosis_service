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
@Table(name="DIAGNOSIS_PROBLEM")
public class DiagnosisProblem {
	
	@Id
	private Integer diagnosisProbId;
	
	private Integer basicProbId;
	
	private Integer upperProbId;
	
	private Integer lowerProbId;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="basicProbId", insertable = false, updatable = false)
	private Problem problem;
}
