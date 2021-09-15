package com.tmax.WaplMath.AdditionalLearning.service.problem;

import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AdditionalLearning.dto.FreqProbCurriDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentProblemDTO;

public interface ProblemServiceBaseV0 {
	
	//소단원 input --> (소단원 , 출제한 적 없는 빈출문제) output
	public List<FreqProbCurriDTO> getNotProvidedFreqProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList);
	
	// 2021-09-15 Add.
	//소단원 input --> (소단원 , 출제한 적 없는 카테고리 문제) output
	public List<FreqProbCurriDTO> getNotProvidedCategoryProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList, String category);
	
	//소단원 input --> (소단원 , 출제한 적 있는 빈출문제) output
	public List<FreqProbCurriDTO> getProvidedFreqProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList);
	
	//소단원 input --> (소단원 ,모든 빈출문제) output
	public List<FreqProbCurriDTO> getAllFreqProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList);

	//(소단원, 빈출문제), 우선순위로 정렬된 소단원 리스트 --> 우선순위 고려하여 각 소단원 골고루 빈출문제 출제
	public List<FrequentProblemDTO> SortingAndRecommend(List<FreqProbCurriDTO> freqProbCurri, List<String> subsectionList , int num);
}
