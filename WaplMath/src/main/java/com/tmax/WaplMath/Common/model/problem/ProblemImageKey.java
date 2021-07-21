package com.tmax.WaplMath.Common.model.problem;

import java.io.Serializable;

import lombok.Data;

@Data
public class ProblemImageKey implements Serializable{
	private Integer probId;
	private String src;
}
