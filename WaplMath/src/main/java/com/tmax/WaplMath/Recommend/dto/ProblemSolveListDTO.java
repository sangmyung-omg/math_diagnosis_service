package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemSolveListDTO {
	public List<String> probIdList;
	public List<String> correctList;
}
