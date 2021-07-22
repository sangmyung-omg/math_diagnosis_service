package com.tmax.WaplMath.AdditionalLearning.service.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.AdditionalLearning.dto.FreqProbCurriDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentProblemDTO;
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
		List<Problem> probEntity = problemRepo.getFrequentNotProvidedProblemByCurri(probIdList, curriculumIdList);
		
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
		
		List<FreqProbCurriDTO> freqProbCurriDTOList =new ArrayList<FreqProbCurriDTO>();
		List<Problem> probEntity = problemRepo.getFrequentProvidedProblemByCurri(probIdList, curriculumIdList);
		
		for(Problem p : probEntity){
			FreqProbCurriDTO dto= new FreqProbCurriDTO();
			dto.setProblemId(p.getProbId());
			dto.setCurriculumId(p.getProblemType().getCurriculumId());
			freqProbCurriDTOList.add(dto);
		}
		
		return freqProbCurriDTOList;
	}
	
	@Override
	public List<FreqProbCurriDTO> getAllFreqProbListBySubsection(Set<Integer> probIdList, List<String> curriculumIdList) {
		
		List<FreqProbCurriDTO> freqProbCurriDTOList =new ArrayList<FreqProbCurriDTO>();
		List<Problem> probEntity = problemRepo.getFrequentAllProblemByCurri(probIdList, curriculumIdList);
		
		for(Problem p : probEntity){
			FreqProbCurriDTO dto= new FreqProbCurriDTO();
			dto.setProblemId(p.getProbId());
			dto.setCurriculumId(p.getProblemType().getCurriculumId());
			freqProbCurriDTOList.add(dto);
		}
		
		return freqProbCurriDTOList;
	}

	@Override
	public List<FrequentProblemDTO> SortingAndRecommend(List<FreqProbCurriDTO> freqProbCurri,
			List<String> subsectionList, int num) {
		
		List<FrequentProblemDTO> recommendFreqProbIdList = new ArrayList<FrequentProblemDTO>();
		
		
		//소단원 이해도 낮은 순서로 골고루 빈출 문제 뽑힐 수 있게 sorting
		int check =0;
		List<FrequentProblemDTO> resultList = new ArrayList<FrequentProblemDTO>();
		while(check<freqProbCurri.size()) {
			for(int i = 0 ; i<subsectionList.size(); i++) {
				for(int j = 0 ; j<freqProbCurri.size(); j++) {
					if(freqProbCurri.get(j).getCurriculumId().equals(subsectionList.get(i))) {
						
						FrequentProblemDTO result = new FrequentProblemDTO();
						result.setProblemId(freqProbCurri.get(j).getProblemId());
						resultList.add(result);
						freqProbCurri.get(j).setCurriculumId("0");
						check++;
						break;
					}
				}
			}
		}
		
		//추천 문제 개수 최대 num개
		for(int i = 0 ; i<num ; i++) {
			
			if(resultList.size()==i)
				break;

			FrequentProblemDTO freqProb = new FrequentProblemDTO();
			freqProb.setProblemId(resultList.get(i).getProblemId());
			recommendFreqProbIdList.add(freqProb);
			
		}
		return recommendFreqProbIdList;
	}

}
