package com.tmax.WaplMath.Recommend.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "UK_REL")
@Data
@IdClass(UkRelKey.class)
public class UkRel {
	@Id
	private String baseUkUuid;
	@Id
	private String preUkUuid;
	@Id
	private String relationReference;
}