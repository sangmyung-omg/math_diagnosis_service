package com.tmax.WaplMath.AdditionalLearning.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.tmax.WaplMath.AdditionalLearning.model.problem.UserFrequentProblem;

import lombok.Data;

@Data
public class FreqProbCurriDTO {
	private int problemId;
	private String curriculumId;
}
