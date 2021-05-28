package com.tmax.WaplMath.Recommend.service.problem;

import java.util.Map;

public interface ProblemServiceBase {
	Map<String, Object> getDiagnosisProblems(String userId, String diagType);
	Map<String, Object> getAdaptiveProblem(String token, String diagType, Integer probId);
}
