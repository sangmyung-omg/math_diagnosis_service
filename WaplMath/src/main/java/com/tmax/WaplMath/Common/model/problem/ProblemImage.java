package com.tmax.WaplMath.Common.model.problem;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "PROBLEM_IMAGE")
@IdClass(ProblemImageKey.class)
public class ProblemImage {

	@Id
	private Integer probId;
	@Id
	private String src;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "probId", referencedColumnName = "probId", insertable = false, updatable = false)
	private Problem problem;

}
