package com.tmax.WaplMath.Common.model.problem;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.tmax.WaplMath.Common.model.uk.Uk;
import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_UK_REL")
@IdClass(ProblemUkRelKey.class)
public class ProblemUkRel {
	@Id
	private Integer probId;
	@Id
	private	Integer ukId;
	
	@ManyToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="probId", insertable = false, updatable = false)
	private Problem problem;
	
	@ManyToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="ukId", insertable = false, updatable = false)
	private Uk uk;
}
