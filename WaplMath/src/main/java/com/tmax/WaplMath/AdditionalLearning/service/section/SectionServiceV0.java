package com.tmax.WaplMath.AdditionalLearning.service.section;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.AdditionalLearning.dto.SubsectionMasteryDTO;
import com.tmax.WaplMath.AdditionalLearning.repository.SectionRepo;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("AddLearn-SectionService")
public class SectionServiceV0 implements SectionServiceBaseV0{

	@Autowired
	@Qualifier("AddLearn-SectionRepo")
	SectionRepo sectionRepo;
	
	
	@Override
	public List<String> getSubsectionListByProblem(String userId, Set<Integer> probIdList) {
		
		List<String> subSectionList = new ArrayList<String>();
		List<String> subSectionAndMastery =  sectionRepo.getCurriculumAndMasteryByProbId(userId,probIdList);
		
		for(String str : subSectionAndMastery) {
			subSectionList.add(str.split(",")[0]);
			//subSectionList.add(str);
		}
		
		return subSectionList;
	}
	
	
	@Override
	public List<String> getSubsectionListByProblemWithScope(String userId, Set<Integer> probIdList, String scopeStart, String scopeEnd) {
		List<String> subSectionList = new ArrayList<String>();
		
		List<String> subSectionAndMastery =  sectionRepo.getCurriculumAndMasteryByProbIdWithScope(userId,probIdList, scopeStart, scopeEnd);
		
		for(String str : subSectionAndMastery) {
			subSectionList.add(str.split(",")[0]);
			//subSectionList.add(str);
		}
		
		return subSectionList;
	}
	

	@Override
	public List<String> getSubsectionListByCurriScope(String userId, String scopeStart, String scopeEnd) {
		List<String> subSectionList = new ArrayList<String>();
		
		List<String> subSectionAndMastery =  sectionRepo.getCurriculumAndMasteryByCurriScope(userId, scopeStart, scopeEnd);
		
		for(String str : subSectionAndMastery) {
			subSectionList.add(str.split(",")[0]);
			//subSectionList.add(str);
		}
		
		return subSectionList;
	}

	@Override
	public List<SubsectionMasteryDTO> getSubsectionMasteryListByProblem(String userId, Set<Integer> probIdList) {
		
		List<SubsectionMasteryDTO> subSectionList = new ArrayList<SubsectionMasteryDTO>();
		
		List<String> subSectionAndMastery =  sectionRepo.getSubsectionAndMasteryByProbId(userId,probIdList);
		
		for(String str : subSectionAndMastery) {
			SubsectionMasteryDTO dto = new SubsectionMasteryDTO();
			dto.setSubsection(str.split(",")[0]);
			dto.setMastery(Float.parseFloat(str.split(",")[1]));
			subSectionList.add(dto);
		}
		
		return subSectionList;
	}




}
