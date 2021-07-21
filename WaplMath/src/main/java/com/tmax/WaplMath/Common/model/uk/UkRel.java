package com.tmax.WaplMath.Common.model.uk;

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
@Table(name = "UK_REL")
@IdClass(UkRelKey.class)
public class UkRel {
	@Id
	private Integer baseUkId;
	@Id
	private Integer preUkId;
	@Id
	private String relationReference;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "baseUkId", referencedColumnName = "ukId", insertable = false, updatable = false)
	private Uk baseUk;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "preUkId", referencedColumnName = "ukId", insertable = false, updatable = false)
	private Uk preUk;
}
