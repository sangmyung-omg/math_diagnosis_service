package com.tmax.WaplMath.AdditionalLearning.service.section;

import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AdditionalLearning.dto.SubsectionMasteryDTO;

public interface SectionServiceBaseV0 {
	
	//문제 input --> 소단원 output (이해도 낮은순으로 정렬, 특정 범위 내에서만)
	public List<String> getSubsectionListByProblem(String userId, Set<Integer> probIdList, String scopeStart, String scopeEnd);

	//범위 input --> 소단원 output (이해도 낮은순으로 정렬)
	public List<String> getSubsectionListByScope(String userId, String scopeStart, String scopeEnd);
	
	//문제 input --> (소단원,이해도) output
	public List<SubsectionMasteryDTO> getSubsectionMasteryListByProblem(String userId, List<Integer> probIdList);

}
