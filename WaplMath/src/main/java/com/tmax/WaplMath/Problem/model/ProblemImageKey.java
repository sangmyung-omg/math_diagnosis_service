package com.tmax.WaplMath.Problem.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ProblemImageKey implements Serializable{
	private Integer probId;
	private String src;
}
