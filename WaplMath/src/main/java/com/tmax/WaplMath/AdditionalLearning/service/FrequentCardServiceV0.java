package com.tmax.WaplMath.AdditionalLearning.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentProblemDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.ProblemDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.SectionMasteryDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.SubsectionDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.SubsectionFreqProbDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.TodaySubsectionListDTO;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSubSectionMastery;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserFrequentProblem;
import com.tmax.WaplMath.AdditionalLearning.repository.UserFrequentProbRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserSubSectionMasteryRepo;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

@Service("FrequentCardServiceV0")
public class FrequentCardServiceV0 implements FrequentCardServiceBase{
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	
	//조건에 맞는 문제 리스트를 반환하는 LRS 메소드
	LRSAPIManager lrsAPIManager = new LRSAPIManager();
	private final Integer MAX_RECENET_STATEMENT_NUM = 100;
	
	@Override
	public List<Integer> getLRSProblemIdList(String userId, String dayFrom, String today, List<String> sourceTypeList) throws Exception{
		
		List<Integer> probIdList = new ArrayList<Integer>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		JsonArray LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateFrom(dayFrom);
		LRSinput.setDateTo(today);
		LRSinput.setSourceTypeList(sourceTypeList);
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(MAX_RECENET_STATEMENT_NUM);

		try {
			LRSResult = lrsAPIManager.getStatementList(LRSinput);
		} catch (Exception e) {
			throw new Exception("LRS Internal Server Error: " + e.getMessage());
		}
		if (LRSResult.size() != 0) {
			for (JsonElement rowElement : LRSResult) {
				JsonObject row = (JsonObject) rowElement;
				String sourceId = row.get("sourceId").getAsString();
				try {
					probIdList.add(Integer.parseInt(sourceId));
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return probIdList;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//문제 리스트를 받아 해당하는 이해도가 낮은 순서로 소단원 리스트 반환
	
	@Autowired
	UserSubSectionMasteryRepo UserSubSecMasteryRepo;
	
	@Override
	public List<String> getSubsectionMasteryOfUser(String userId, List<Integer> probIdList){
		
		List<String> recentSubsectionIdList = new ArrayList<String>();
		
		//최근(14일)동안 공부했던 소단원에 대한 평균 이해도 ==> 이해도 낮은 소단원에 대한 빈출문제 출제할 것
		List<UserSubSectionMastery> subsectionList = UserSubSecMasteryRepo.getSubSectionAndMastery(userId,probIdList);
		logger.info("\n최근 공부한 소단원과 평균 이해도 리스트 : " + subsectionList);
		
		for(int i = 0 ; i<subsectionList.size() ; i++) {
			recentSubsectionIdList.add(subsectionList.get(i).getCurriculumId());
		}
		
		//공부한 소단원이 없는 경우 - query 에러 방지 위해 없는 curriculumId = 0 을 추가
		if(subsectionList.size()==0) {
			recentSubsectionIdList.add("0");
		}
		
		return recentSubsectionIdList;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//소단원 리스트를 받아 해당하는 빈출문제를 반환
	//input : 1.최근 공부한 소단원 --> 출제한적없는(not fix) 2.오늘 공부할 소단원(오늘의 학습 카드) --> 출제한적없는 / 출제한적있는
	//각 소단원마다 빈출 문제 골고루 뽑는 로직 포함
	
	@Autowired
	UserFrequentProbRepo UserFreqProbRepo;
	
	@Override
	public List<FrequentProblemDTO> getSubsectionFreqProb(List<String> subsectionList, TodaySubsectionListDTO todaySubsectionList, List<Integer> solvedProbIdList){
		List<FrequentProblemDTO> recommendFreqProbIdList = new ArrayList<FrequentProblemDTO>();
		FrequentProblemDTO recommendFreqProbId = new FrequentProblemDTO();
		
		List<UserFrequentProblem> todayProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todaySubsectionList.getTodaySubsectionList());
		logger.info("\n오늘 배울 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
		List<UserFrequentProblem> recentProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,subsectionList);
		logger.info("\n최근 공부한 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + recentProbList);
		
		//+최근 공부한 소단원에 대한 출제한 적 있는 빈출 문제 리스트
		
		
		
		//각 소단원마다 빈출 문제 골고루 뽑는 로직. 순서는 이해도 낮은 소단원 순서로 
		logger.info("\n최근 공부한 소단원 이해도 낮은 순 리스트 : " + subsectionList);
		
		//(출제한적없는)
		int check =0;
		List<UserFrequentProblem> resultList = new ArrayList<UserFrequentProblem>(recentProbList.size());
		while(check<recentProbList.size()) {
			for(int i = 0 ; i<subsectionList.size(); i++) {
				for(int j = 0 ; j<recentProbList.size(); j++) {
					if(recentProbList.get(j).getCurriculumId().equals(subsectionList.get(i))) {
						
						UserFrequentProblem result = new UserFrequentProblem();
						result.setProblemId(recentProbList.get(j).getProblemId());
						result.setCurriculumId(recentProbList.get(j).getCurriculumId());
						resultList.add(result);
						recentProbList.get(j).setCurriculumId("0");
						check++;
						break;
					}
				}
			}
		}
		
		logger.info("\n최근공부 소단원 - 이해도 낮은 순서로 골고루 (출제한적없는)빈출 문제 뽑힐 수 있게 sorting : " + resultList);
		
		//+(출제한적있는)
			
		
		//(출제한적없는)추천 문제 개수 최대 3개
		for(int i = 0 ; i<3 ; i++) {
			
			if(resultList.size()==i)
				break;
			
			FrequentProblemDTO freqProb = new FrequentProblemDTO();
			freqProb.setProblemId(resultList.get(i).getProblemId());
			recommendFreqProbIdList.add(freqProb);
			
		}
		
		logger.info("\n추천하고자 하는 (출제한적없는)빈출 문제 리스트 : " + recommendFreqProbIdList);
		
		//+(출제한적있는)추천 문제 개수 최대 3개
		
		
		
		return recommendFreqProbIdList;
	};
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//추천된 빈출문제 리스트를 받아 해당하는 중단원과 이해도를 반환
	@Override
	public List<SectionMasteryDTO> getSectionMasteryOfUser(String userId, List<Integer> probIdList){
		
		return null;
	};
	

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public FrequentCardDTO getFrequentCard(String userId, TodaySubsectionListDTO todaySubsectionList) {
		
		//반환할 빈출 카드
		FrequentCardDTO FrequentCard = new FrequentCardDTO();
		
		//과거 14일동안 풀었던 문제 리스트(LRS) ==> 최근 어떤 소단원을 공부했는지 확인
		List<Integer> recentSolvedProbIdList = new ArrayList<Integer>();
		
		//그동안 풀었던 문제 리스트(LRS) ==> 문제 중복 출제 방지
		List<Integer> solvedProbIdList = new ArrayList<Integer>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String today = LocalDate.now().format(formatter);
		String Fromday = LocalDate.now().minusDays(14).format(formatter);
		
		List<String> sourceTypeList = new ArrayList<String>(
				Arrays.asList("question")); // 진단고사, 추가학습(빈출문제카드)에서 푼 문제를 제외

		try {
			//recentSolvedProbIdList = this.getLRSProblemIdList(userId, Fromday, today, sourceTypeList);
			//solvedProbIdList = this.getLRSProblemIdList(userId, Fromday, today, sourceTypeList); //??????Fromday값 뭘로 넣어줘야함?
			
			/*임시                          */
			recentSolvedProbIdList.add(100);
			recentSolvedProbIdList.add(200);
			recentSolvedProbIdList.add(300);
			solvedProbIdList.add(0);
			/*                            */
			
			//문제 푼 기록이 없는 경우 - query 에러 방지 위해 없는 probId = 0 을 추가
			if(recentSolvedProbIdList.size()==0)
				recentSolvedProbIdList.add(0);
			if(solvedProbIdList.size()==0)
				solvedProbIdList.add(0);
			
		} catch (Exception e) {
			FrequentCard.setResultMessage(e.getMessage());
			return FrequentCard;
		}
		
		logger.info("\n과거 14일동안 풀었던 probId 리스트 : " + recentSolvedProbIdList);
		logger.info("\n그동안 풀었던 probId 리스트 : " + solvedProbIdList);

		
		//과거 14일 동안 공부한 소단원리스트(이해도 낮은 순)
		List<String> recentSectionList = new ArrayList<String>();
		recentSectionList = getSubsectionMasteryOfUser(userId, recentSolvedProbIdList);
		
		
		//추천할 빈출 문제
		List<FrequentProblemDTO> recommendFreqProbList = new ArrayList<FrequentProblemDTO>();
		recommendFreqProbList = getSubsectionFreqProb(recentSectionList, todaySubsectionList, solvedProbIdList);
		
		
		//빈출 문제들에 해당하는 중단원들과 이해도
		List<Integer> recommendFreqProbIdList = new  ArrayList<Integer>();
		List<SectionMasteryDTO> sectionMasteryList = new ArrayList<SectionMasteryDTO>();
		sectionMasteryList = getSectionMasteryOfUser(userId, recommendFreqProbIdList);
		
		
		
		//최종 빈출 카드 구성
		FrequentCard.setCardType("빈출카드");
		FrequentCard.setProbSetList(recommendFreqProbList);
		FrequentCard.setSectionSetList(sectionMasteryList);
		return FrequentCard;
	}

}
