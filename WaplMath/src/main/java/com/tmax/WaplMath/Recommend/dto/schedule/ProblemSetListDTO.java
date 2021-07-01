package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemSetListDTO {
	private List<Integer> high;
	private List<Integer> middle;
	private List<Integer> low;
}
