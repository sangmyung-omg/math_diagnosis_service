package com.tmax.WaplMath.Recommend.dto.waplscore;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WaplScoreProbDTO {
	public List<Integer> ukList;
	public String type;
}
