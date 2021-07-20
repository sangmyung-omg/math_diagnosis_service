package com.tmax.WaplMath.AdditionalLearning.service.problem;

import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AdditionalLearning.dto.FreqProbCurriDTO;

public interface ProblemServiceBaseV0 {
	
	//소단원 input --> (소단원 , 출제한 적 없는 빈출문제) output
	public List<FreqProbCurriDTO> getNotProvidedFreqProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList);
	
	//소단원 input --> (소단원 , 출제한 적 있는 빈출문제) output
	public List<FreqProbCurriDTO> getProvidedFreqProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList);


}
