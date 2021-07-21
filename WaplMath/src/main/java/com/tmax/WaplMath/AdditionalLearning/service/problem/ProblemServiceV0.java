package com.tmax.WaplMath.AdditionalLearning.service.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.AdditionalLearning.dto.FreqProbCurriDTO;
import com.tmax.WaplMath.AdditionalLearning.repository.ProblemRepo;
import com.tmax.WaplMath.Common.model.problem.Problem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("AddLearn-ProblemService")
public class ProblemServiceV0 implements ProblemServiceBaseV0{
	
	@Autowired
	@Qualifier("AddLearn-ProblemRepo")
	ProblemRepo problemRepo;

	@Override
	public List<FreqProbCurriDTO> getNotProvidedFreqProbListBySubsection(Set<Integer> probIdList,
			List<String> curriculumIdList) {
		
		List<FreqProbCurriDTO> freqProbCurriDTOList =new ArrayList<FreqProbCurriDTO>();
		List<Problem> probEntity = problemRepo.getFrequentNotProvidedProblem(probIdList, curriculumIdList);
		
		for(Problem p : probEntity){
			FreqProbCurriDTO dto= new FreqProbCurriDTO();
			dto.setProblemId(p.getProbId());
			dto.setCurriculumId(p.getProblemType().getCurriculumId());
			freqProbCurriDTOList.add(dto);
		}
		
		return freqProbCurriDTOList;
	}

	@Override
	public List<FreqProbCurriDTO> getProvidedFreqProbListBySubsection(Set<Integer> probIdList,
			List<String> curriculumIdList) {
		// TODO Auto-generated method stub
		return null;
	}

}
