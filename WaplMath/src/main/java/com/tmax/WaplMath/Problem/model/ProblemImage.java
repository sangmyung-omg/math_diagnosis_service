package com.tmax.WaplMath.Problem.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_IMAGE")
@IdClass(ProblemImageKey.class)
public class ProblemImage {
	
	@Id
	private Integer probId;
	@Id
	private String src;
	
}
