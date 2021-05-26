package com.tmax.WaplMath.Problem.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM_IMAGE")
public class ProblemImage {
	
	@Id
	private int probId;
	private String src;
	
}
