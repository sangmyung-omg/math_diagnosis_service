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
import com.tmax.WaplMath.AdditionalLearning.dto.SectionMasteryDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.TodaySubsectionListDTO;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSubSectionMastery;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserTargetExamScope;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserFrequentProblem;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSectionMastery;
import com.tmax.WaplMath.AdditionalLearning.repository.UserFrequentProbRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserSectionMasteryRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserSubSectionMasteryRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserTargetExamScopeRepo;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

@Service("FrequentCardServiceV1")
public class FrequentCardServiceV1 implements FrequentCardServiceBaseV1{
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	
	//조건에 맞는 문제 리스트를 반환하는 LRS 메소드
	@Autowired
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
	@Autowired
	UserTargetExamScopeRepo UserTargetExamScopeRepo ;
	
	@Override
	public List<String> getSubsectionMasteryOfUser(String userId, boolean isFirstFreq, List<Integer> probIdList){
		
		List<String> recentSubsectionIdList = new ArrayList<String>();
		
		//타겟시험 범위
		UserTargetExamScope targetExamScope = UserTargetExamScopeRepo.getUserTargetExamScope(userId);
		String start;
		String end;
		
		//진단고사용 빈출 카드는 출제 범위를 타겟시험으로 제한하지 않음 
		if(isFirstFreq) {
			start = "중등-중1-1학-01-01-01";
			end = "중등-중3-2학-03-02-01";
		}else {
			
			//임시 에러 방지
			if(targetExamScope==null) {
				start = "중등-중1-1학-01-01-01";
				end = "중등-중3-2학-03-02-01";
			}else {
				start = targetExamScope.getStartSubSectionId();
				end = targetExamScope.getEndSubSectionId();
			}
			
		}
		
	
		
		//최근(14일)동안 공부했던 소단원에 대한 평균 이해도 ==> 이해도 낮은 소단원에 대한 빈출문제 출제할 것
		//타겟시험범위 내 소단원만 도출
		List<UserSubSectionMastery> subsectionList = UserSubSecMasteryRepo.getSubSectionAndMastery(start,end,userId,probIdList);
		logger.info("\n소단원과 평균 이해도 리스트 : " + subsectionList);
		
		for(int i = 0 ; i<subsectionList.size() ; i++) {
			recentSubsectionIdList.add(subsectionList.get(i).getCurriculumId());
		}
		
		return recentSubsectionIdList;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//소단원 리스트를 받아 해당하는 빈출문제를 반환
	//input : 1.최근 14일동안 공부한 소단원 --> 출제한적없는 / 출제한적있는  2.오늘 소단원(진단고사 or 오늘의 학습 카드) --> 출제한적없는
	//각 소단원마다 빈출 문제 골고루 뽑는 로직 포함
	@Autowired
	UserFrequentProbRepo UserFreqProbRepo;
	
	
	@Override
	public List<FrequentProblemDTO> getSubsectionFreqProb(String userId, boolean isFirstFreq, List<String> diagnosisSubsectionList, List<String> subsectionList, List<String> todayCardSubsectionList, List<Integer> solvedProbIdList){
		List<FrequentProblemDTO> recommendFreqProbIdList = new ArrayList<FrequentProblemDTO>();
		
		if(isFirstFreq) 
		{
			//초등 소단원 제외한 진단고사에서 학습한 소단원
			List<String> notEleDiagnosisSubsectionList = new ArrayList<String>();
			
			//진단고사를 하나라도 푼 경우
			if(diagnosisSubsectionList.size()!=0) {
				for(int i =0 ; i<diagnosisSubsectionList.size(); i++) {
					if(!diagnosisSubsectionList.get(i).contains("초등")) {
						notEleDiagnosisSubsectionList.add(diagnosisSubsectionList.get(i));
					}
				}
			}
			
			if(notEleDiagnosisSubsectionList.size()!=0) {
				List<UserFrequentProblem> todayProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,notEleDiagnosisSubsectionList);
				logger.info("\n진단고사 내 중등 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
			
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,5));
			}else {
				List<UserFrequentProblem> todayProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todayCardSubsectionList);
				logger.info("\n(진단고사를 봤는데-모두 초등 소단원 or 한 문제도 풀지 않음,)오늘 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
				
				//진단고사 소단원 대체할 오늘 소단원
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,5));
			}
		}
		
		else 
		{
			//최근 공부한 소단원이 있다면
			if(subsectionList.size()!=0) {
				
				
				List<UserFrequentProblem> todayProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todayCardSubsectionList);
				logger.info("\n(최근 공부한 소단원이 존재할 때,)오늘 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
				
				//오늘 소단원
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,1));
				
				
				List<UserFrequentProblem> recentProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,subsectionList);
				logger.info("\n최근 공부한 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + recentProbList);
				
				List<UserFrequentProblem> providedRecentProbList = UserFreqProbRepo.getFrequentProvidedProblem(solvedProbIdList,subsectionList);
				logger.info("\n최근 공부한 소단원에 대한 출제한 적 있는 빈출 문제 리스트 : " + providedRecentProbList);
				
				
				//최근소단원 - 출제X
				recommendFreqProbIdList.addAll(SortingAndRecommend(recentProbList,subsectionList,2));
				//최근소단원 - 출제O
				recommendFreqProbIdList.addAll(SortingAndRecommend(providedRecentProbList,subsectionList,2));
				
				//최소 3문제를 확보하지 못한다면 과거 14일 기준 가장 예전에 배운 소단원보다 더 오래된 소단원들에서 빈출문제 선정 
				if(recommendFreqProbIdList.size()<3) {
					
					
					List<String> AnothersubsectionList = new ArrayList<String>();
					AnothersubsectionList.addAll(getAnotherSubsectionMasteryOfUser(userId,subsectionList));
					
					List<UserFrequentProblem> probList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,AnothersubsectionList);
					recommendFreqProbIdList.addAll(SortingAndRecommend(probList,AnothersubsectionList,3-recommendFreqProbIdList.size()));
					
					if(recommendFreqProbIdList.size()<3) {
						List<UserFrequentProblem> providedProbList = UserFreqProbRepo.getFrequentProvidedProblem(solvedProbIdList,AnothersubsectionList);
						recommendFreqProbIdList.addAll(SortingAndRecommend(providedProbList,AnothersubsectionList,3-recommendFreqProbIdList.size()));
					}
				}
				
			}else{
				
				List<UserFrequentProblem> todayProbList = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todayCardSubsectionList);
				logger.info("\n(최근 공부한 소단원이 존재하지 않을 때,)오늘 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
				
				//오늘 소단원
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,5));
			}
			
		}
		
		logger.info("\n최종 추천 빈출 문제 리스트 : " + recommendFreqProbIdList);
		
		return recommendFreqProbIdList;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//각 소단원마다 빈출 문제 골고루 뽑는 로직
	public List<FrequentProblemDTO> SortingAndRecommend(List<UserFrequentProblem> freqProbSec, List<String> subsectionList , int num) {
		
		logger.info("\n빈출문제 - 소단원 매핑 set : " + freqProbSec);
		logger.info("\n이해도 낮은 순서로 정렬된 소단원 리스트 : " + subsectionList);
		
		List<FrequentProblemDTO> recommendFreqProbIdList = new ArrayList<FrequentProblemDTO>();

		//소단원 이해도 낮은 순서로 골고루 빈출 문제 뽑힐 수 있게 sorting
		int check =0;
		List<UserFrequentProblem> resultList = new ArrayList<UserFrequentProblem>(freqProbSec.size());
		while(check<freqProbSec.size()) {
			for(int i = 0 ; i<subsectionList.size(); i++) {
				for(int j = 0 ; j<freqProbSec.size(); j++) {
					if(freqProbSec.get(j).getCurriculumId().equals(subsectionList.get(i))) {
						
						UserFrequentProblem result = new UserFrequentProblem();
						result.setProblemId(freqProbSec.get(j).getProblemId());
						result.setCurriculumId(freqProbSec.get(j).getCurriculumId());
						resultList.add(result);
						freqProbSec.get(j).setCurriculumId("0");
						check++;
						break;
					}
				}
			}
		}
		logger.info("\n소단원 이해도 낮은 순서로 골고루 빈출 문제 뽑힐 수 있게 sorting : " + resultList);
		

		//추천 문제 개수 최대 num개
		for(int i = 0 ; i<num ; i++) {
			
			if(resultList.size()==i)
				break;

			FrequentProblemDTO freqProb = new FrequentProblemDTO();
			freqProb.setProblemId(resultList.get(i).getProblemId());
			recommendFreqProbIdList.add(freqProb);
			
		}
		logger.info("\n추천하고자 하는 빈출 문제 리스트 : " + recommendFreqProbIdList);
		
			
		return recommendFreqProbIdList;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//문제 부족 시, 범위를 늘려서 소단원 뽑는 로직
	public List<String> getAnotherSubsectionMasteryOfUser(String userId, List<String> subsectionList){
		
		List<String> AnotherSubsectionIdList = new ArrayList<String>();
		
		//타겟시험 범위
		UserTargetExamScope targetExamScope = UserTargetExamScopeRepo.getUserTargetExamScope(userId);
		String start;
		String end;
		
		//임시 에러 방지
		if(targetExamScope==null) {
			start = "중등-중1-1학-01-01-01";
		}else {
			start = targetExamScope.getStartSubSectionId();
		}
		
		subsectionList.sort(null);
		end = subsectionList.get(0);
		logger.info("\n오름차순 정렬 확인 : " + subsectionList);
		//subsectionList.sort(Comparator.reverseOrder());
		
		
		
		//타겟시험범위시작 ~ 최근(14일)동안 공부했던 가장 오래전 소단원   내의 소단원과 평균 이해도 ==> 이해도 낮은 소단원에 대한 빈출문제 출제할 것
		List<UserSubSectionMastery> AnotherSubsectionList = UserSubSecMasteryRepo.getAnotherSubSectionAndMastery(start,end,userId,subsectionList);
		logger.info("\n타겟시험범위시작 ~ 최근(14일)동안 공부했던 가장 오래전 소단원 : 소단원과 평균 이해도 리스트 : " + AnotherSubsectionList);
		
		for(int i = 0 ; i<AnotherSubsectionList.size() ; i++) {
			AnotherSubsectionIdList.add(AnotherSubsectionList.get(i).getCurriculumId());
		}
		
		return AnotherSubsectionIdList;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//추천된 빈출문제 리스트를 받아 해당하는 중단원과 이해도를 반환
	
	@Autowired
	UserSectionMasteryRepo UserSectionMasteryRepo;
	
	@Override
	public List<SectionMasteryDTO> getSectionMasteryOfUser(String userId, List<FrequentProblemDTO> probIdList){
		
		List<SectionMasteryDTO> sectionMasteryList = new ArrayList<SectionMasteryDTO>();
		
		List<Integer> probList = new ArrayList<Integer>();
		for(int i =0; i<probIdList.size();i++) {
			probList.add(probIdList.get(i).getProblemId());
		}
		
		List<UserSectionMastery> userSectionMasteryList = UserSectionMasteryRepo.getSubSectionAndMastery(userId, probList);
		for(int i = 0 ; i<userSectionMasteryList.size() ; i++) {
			SectionMasteryDTO sectionMastery = new SectionMasteryDTO();
			sectionMastery.setSection(userSectionMasteryList.get(i).getSection());
			sectionMastery.setMastery(userSectionMasteryList.get(i).getUkMastery());
			
			sectionMasteryList.add(sectionMastery);
		}
		
		
		return sectionMasteryList;
	};
	

	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
	public FrequentCardDTO getFrequentCard(String userId, boolean isFirstFrequent) {
		
		boolean isFirstFreq = isFirstFrequent;
		
		//반환할 빈출 카드
		FrequentCardDTO FrequentCard = new FrequentCardDTO();
		
		//과거 14일동안 풀었던 문제 리스트(LRS) ==> 최근 어떤 소단원을 공부했는지 확인 (진단고사, 추가학습(빈출문제카드)에서 푼 문제를 제외)
		List<Integer> recentSolvedProbIdList = new ArrayList<Integer>();
		
		//그동안 풀었던 문제 리스트(LRS) ==> 문제 중복 출제 방지 (그동안 풀었던 빈출문제 확인해야되니 모든 문제를 확인해야)
		List<Integer> solvedProbIdList = new ArrayList<Integer>();
		
		
		//과거 14일 동안 공부한 소단원리스트(이해도 낮은 순)
		List<String> recentSectionList = new ArrayList<String>();
		
		
		//오늘의 학습 카드에서 학습한 문제 리스트(LRS)
		List<Integer> todayCardProbIdList = new ArrayList<Integer>();
		//오늘의 학습 카드에서 학습한 소단원 리스트
		List<String> todayCardSubsectionList = new ArrayList<String>();
		
		//진단고사에서 학습한 문제 리스트(LRS)
		List<Integer> diagnosisProbIdList = new ArrayList<Integer>();
		//진단고사에서 학습한 소단원 리스트
		List<String> diagnosisSubsectionList = new ArrayList<String>();
		
		
	
		//진단고사 응시 후 최초 접속
		//진단고사에서 출제된 소단원을 기준으로 문제 추천
		if(isFirstFreq) 
		{
			logger.info("\n진단고사 응시 후 완벽 학습 최초 접속");
			
			solvedProbIdList.add(0); // query에러 방지
			recentSectionList = null;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String today = LocalDate.now().format(formatter);
			
			List<String> sourceTypeList = new ArrayList<String>(
					Arrays.asList("diagnosis")); // 진단고사 문제

			try {
				diagnosisProbIdList = this.getLRSProblemIdList(userId, today, null, sourceTypeList);
				if(diagnosisProbIdList.size()==0) {
					FrequentCard.setResultMessage("No diagnosis problems in LRS.");
				}
				
			} catch (Exception e) {
				FrequentCard.setResultMessage(e.getMessage());
				return FrequentCard;
			}
			
			logger.info("\n진단고사에서 풀었던 probId 리스트 : " + diagnosisProbIdList);
			
			diagnosisSubsectionList = getSubsectionMasteryOfUser(userId, isFirstFreq, diagnosisProbIdList);
			
		}
		
		//두번째 학습날 부터
		else 
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String today = LocalDate.now().format(formatter);
			String Fromday = LocalDate.now().minusDays(14).format(formatter);
			
			List<String> sourceTypeList = new ArrayList<String>(
					Arrays.asList("question","type_question","supple_question","mid_exam_question","trial_exam_question")); // 진단고사, 추가학습(빈출문제카드)에서 푼 문제를 제외 - 오늘의 학습 카드 source type
			List<String> sourceTypeListAll = new ArrayList<String>(
					Arrays.asList("question","type_question","supple_question","mid_exam_question","trial_exam_question","diagnosis","frequent_question")); // 모든 source type

			try {
				recentSolvedProbIdList = this.getLRSProblemIdList(userId, Fromday, today, sourceTypeList);
				solvedProbIdList = this.getLRSProblemIdList(userId, null, today, sourceTypeListAll); 
				todayCardProbIdList = this.getLRSProblemIdList(userId, today, null, sourceTypeList);
				
				if(todayCardProbIdList.size()==0) {
					FrequentCard.setResultMessage("No today-card problems in LRS.");
				}
				
			} catch (Exception e) {
				FrequentCard.setResultMessage(e.getMessage());
				return FrequentCard;
			}
			
			/*
			// query에러 방지
			if(recentSolvedProbIdList.size()==0) {
				recentSolvedProbIdList.add(0);
			}
			if(solvedProbIdList.size()==0) {
				solvedProbIdList.add(0);
			}
			*/
			
			logger.info("\n과거 14일동안 풀었던 probId 리스트 : " + recentSolvedProbIdList);
			logger.info("\n그동안 풀었던 probId 리스트 : " + solvedProbIdList);
			logger.info("\n오늘의 학습 카드에서 풀었던 probId 리스트 : " + todayCardProbIdList);
			
			

			recentSectionList = getSubsectionMasteryOfUser(userId, isFirstFreq, recentSolvedProbIdList);
			todayCardSubsectionList = getSubsectionMasteryOfUser(userId, isFirstFreq, todayCardProbIdList);
		}
		

		//추천할 빈출 문제
		List<FrequentProblemDTO> recommendFreqProbList = new ArrayList<FrequentProblemDTO>();
		recommendFreqProbList = getSubsectionFreqProb(userId, isFirstFreq, diagnosisSubsectionList, recentSectionList, todayCardSubsectionList, solvedProbIdList);
		if(recommendFreqProbList.size()!=0) {
			FrequentCard.setResultMessage("Successfully return frequent card.");
		}
		
		
		//빈출 문제들에 해당하는 중단원들과 이해도
		List<SectionMasteryDTO> sectionMasteryList = new ArrayList<SectionMasteryDTO>();
		sectionMasteryList = getSectionMasteryOfUser(userId, recommendFreqProbList);
		
		
		
		//최종 빈출 카드 구성
		FrequentCard.setCardType("빈출카드");
		FrequentCard.setProbSetList(recommendFreqProbList);
		FrequentCard.setSectionSetList(sectionMasteryList);
		return FrequentCard;
	}

}
