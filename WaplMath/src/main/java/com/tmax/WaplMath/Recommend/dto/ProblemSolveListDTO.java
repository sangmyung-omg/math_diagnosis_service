package com.tmax.WaplMath.Recommend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemSolveListDTO {
	public List<String> probIdList;
	public List<String> correctList;
}
