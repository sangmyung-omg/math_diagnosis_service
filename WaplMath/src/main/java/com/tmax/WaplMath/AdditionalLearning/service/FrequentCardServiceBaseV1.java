package com.tmax.WaplMath.AdditionalLearning.service;

import java.util.List;

import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentProblemDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.SectionMasteryDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.TodaySubsectionListDTO;

public interface FrequentCardServiceBaseV1 {
	
	
	//조건에 맞는 문제 리스트를 반환하는 LRS 메소드
	//1.14일동안 푼 문제(최근 공부한 소단원 도출) 2.오늘 푼 문제(진단고사 or 오늘의 학습) 3.그동안 푼 문제(문제 중복 출제 방지) 
	List<Integer> getLRSProblemIdList(String userId, String dayFrom, String today, List<String> sourceTypeList)throws Exception;
	
	//문제 리스트를 받아 해당하는 이해도가 낮은 순서로 소단원 리스트 반환
	List<String> getSubsectionMasteryOfUser(String userId, boolean isFirstFreq, List<Integer> probIdList);
	
	//소단원 리스트를 받아 해당하는 빈출문제를 반환
	//각 소단원마다 빈출 문제 골고루 뽑는 로직 포함
	//문제 부족 시, 범위를 늘려서 소단원 뽑는 로직 포함
	List<FrequentProblemDTO> getSubsectionFreqProb(String userId, boolean isFirstFreq, List<String> diagnosisSubsectionList, List<String> subsectionList, List<String> todaySubsectionList, List<Integer> solvedProbIdList)throws Exception;
	
	//소단원 리스트를 받아 해당하는 중단원과 이해도를 반환
	//굳이 추천된 빈출문제와 매핑 시키진 않을 것
	List<SectionMasteryDTO> getSectionMasteryOfUser(String userId, List<FrequentProblemDTO> probIdList);
	
	//빈출카드 반환
	FrequentCardDTO getFrequentCard(String userId, boolean isFirstFrequent);
	

}
