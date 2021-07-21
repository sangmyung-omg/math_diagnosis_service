package com.tmax.WaplMath.Common.model.problem;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
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
	
	private String frequent;
	
	@ManyToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="curriculumId", insertable = false, updatable = false)
	private Curriculum curriculum;
}
