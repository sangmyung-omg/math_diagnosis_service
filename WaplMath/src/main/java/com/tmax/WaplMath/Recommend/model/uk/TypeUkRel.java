package com.tmax.WaplMath.Recommend.model.uk;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.problem.ProblemType;

import lombok.Data;

@Data
@Entity
@Table(name = "TYPE_UK_REL")
@IdClass(TypeUkRelKey.class)
public class TypeUkRel {
	@Id
	private Integer typeId;
	@Id
	private Integer ukId;
	
	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name="typeId", insertable = false, updatable = false)
	private ProblemType problemType;
	
	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name="ukId", insertable = false, updatable = false)
	private Uk uk;
	
}
