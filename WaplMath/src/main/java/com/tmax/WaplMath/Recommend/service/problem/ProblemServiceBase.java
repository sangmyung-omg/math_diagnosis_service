package com.tmax.WaplMath.Recommend.service.problem;

import java.util.List;
import java.util.Map;

public interface ProblemServiceBase {
	Map<String, Object> getDiagnosisProblems(String userId, String diagType);
	Map<String, Object> getExtraProblem(String token, List<Integer> probIdList);
	public Map<String, Object> getDiagnosisScope(String userId);
	public Map<String, Object> getDiagnosisProblemsBySeciontIdList(List<String> sectionIdList, String diagType);
}
