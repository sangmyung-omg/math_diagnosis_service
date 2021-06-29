package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProblemSolveListDTO {
	public List<String> probIdList;
	public List<String> correctList;
}
