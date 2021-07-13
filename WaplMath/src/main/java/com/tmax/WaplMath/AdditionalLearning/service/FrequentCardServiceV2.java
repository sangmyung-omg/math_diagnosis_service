package com.tmax.WaplMath.AdditionalLearning.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.slf4j.log;
//import org.slf4j.logFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.AdditionalLearning.dto.FreqProbCurriDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentProblemDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.SubsectionMasteryDTO;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSubSectionMastery;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserTargetExamScope;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserFrequentProblem;
import com.tmax.WaplMath.AdditionalLearning.model.problem.UserSectionMastery;
import com.tmax.WaplMath.AdditionalLearning.repository.EstimatedTimeRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserFrequentProbRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserSectionMasteryRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserSubSectionMasteryRepo;
import com.tmax.WaplMath.AdditionalLearning.repository.UserTargetExamScopeRepo;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("FrequentCardServiceV2")
public class FrequentCardServiceV2 implements FrequentCardServiceBaseV2{
	//private final log log = logFactory.getlog(this.getClass().getSimpleName());
	
	
	//조건에 맞는 문제 리스트를 반환하는 LRS 메소드
	@Autowired
	LRSAPIManager lrsAPIManager = new LRSAPIManager();
	private final Integer MAX_RECENET_STATEMENT_NUM = 300;
	
	@Override
	public Set<Integer> getLRSProblemIdList(String userId, String today, String dateFrom, List<String> sourceTypeList) throws Exception {
		Set<Integer> probIdSet = new HashSet<Integer>();
		//List<Integer> probIdList = new ArrayList<Integer>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		List<StatementDTO> LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(today);
		if (dateFrom != "")
			LRSinput.setDateFrom(dateFrom);
		LRSinput.setSourceTypeList(sourceTypeList);
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(MAX_RECENET_STATEMENT_NUM);
		
		try {
			LRSResult = lrsAPIManager.getStatementListNew(LRSinput);
		} catch (Exception e) {
			throw new Exception("LRS Internal Server Error: " + e.getMessage());
		}
		if (LRSResult.size() != 0) {
			for (StatementDTO statement : LRSResult) {
				String sourceId = statement.getSourceId();
				try {
					probIdSet.add(Integer.parseInt(sourceId));
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return probIdSet;
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
				//start = "중등-중1-1학-01-01-01";
				end = "중등-중3-2학-03-02-01";
			}
			
		}
		
		
		//input 문제에 해당하는 소단원에 대한 평균 이해도 ==> 이해도 낮은 소단원에 대한 빈출문제 출제할 것
		//타겟시험범위 내 소단원만 도출
		List<UserSubSectionMastery> subsectionList = UserSubSecMasteryRepo.getSubSectionAndMastery(start,end,userId,probIdList);
		log.info("\n소단원과 평균 이해도 리스트 : " + subsectionList);
		
		for(int i = 0 ; i<subsectionList.size() ; i++) {
			recentSubsectionIdList.add(subsectionList.get(i).getCurriculumId());
		}
		
		return recentSubsectionIdList;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//소단원 리스트를 받아 해당하는 빈출문제를 반환
	//1. 진단고사 직후 : 진단고사 소단원 리스트 --> 출제한 적 없는
	//2. 그 이후 : 오늘의학습카드 소단원 리스트 --> 출제한 적 없는  /  최근 14일동안 공부한 소단원 --> 출제한적없는 & 출제한적있는
	//각 소단원마다 빈출 문제 골고루 뽑는 로직 포함
	//문제 부족 시, 범위를 늘려서 소단원 뽑는 로직 포함
	@Autowired
	UserFrequentProbRepo UserFreqProbRepo;
	
	
	public List<FreqProbCurriDTO> entityToDto (List<UserFrequentProblem> model){
		
		List<FreqProbCurriDTO> dtolist = new ArrayList<FreqProbCurriDTO>();
		for(int i =0 ; i<model.size();i++) {
			FreqProbCurriDTO dto = new FreqProbCurriDTO();
			dto.setProblemId(model.get(i).getProblemId());
			dto.setCurriculumId(model.get(i).getCurriculumId());
			dtolist.add(dto);
		}
		return dtolist;
	}
	
	@Override
	public List<FrequentProblemDTO> getSubsectionFreqProb
	(String userId, boolean isFirstFreq, List<String> diagnosisSubsectionList, List<String> subsectionList, List<String> todayCardSubsectionList, List<Integer> solvedProbIdList) throws Exception{
		List<FrequentProblemDTO> recommendFreqProbIdList = new ArrayList<FrequentProblemDTO>();
		
		//1. 진단고사 직후
		if(isFirstFreq) 
		{
			//초등 소단원 제외한 진단고사에서 학습한 소단원
			List<String> notEleDiagnosisSubsectionList = new ArrayList<String>();
			
			if(diagnosisSubsectionList.size()!=0) {
				for(int i =0 ; i<diagnosisSubsectionList.size(); i++) {
					if(!diagnosisSubsectionList.get(i).contains("초등")) {
						notEleDiagnosisSubsectionList.add(diagnosisSubsectionList.get(i));
					}
				}
			}
			
			//진단고사에서 학습한 소단원이 존재하는 경우
			if(notEleDiagnosisSubsectionList.size()!=0) {
				List<UserFrequentProblem> todayProbList_m = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,notEleDiagnosisSubsectionList);
				List<FreqProbCurriDTO> todayProbList = entityToDto(todayProbList_m);
				log.info("\n진단고사 내 중등 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
			
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,notEleDiagnosisSubsectionList,5));
			}
			//진단고사에서 학습한 소단원이 존재하지 않는 경우 --> 기획적으로 존재하지 않음
			else {
				
				try {
					
					List<UserFrequentProblem> todayProbList_m = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todayCardSubsectionList);
					List<FreqProbCurriDTO> todayProbList = entityToDto(todayProbList_m);
					log.info("\n(진단고사를 봤는데-모두 초등 소단원 or 한 문제도 풀지 않음,)오늘의학습카드 내 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
					
					//진단고사 소단원 대체할 오늘의학습카드 내 소단원
					recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,5));
				}
				catch(Exception e){
					throw new Exception("No Today Card record in LRS: " + e.getMessage());
				}
				
			}
		}
		
		//2. 그 이후
		else 
		{
			//최근 공부한 소단원이 있다면
			if(subsectionList.size()!=0) {
				
				
				List<UserFrequentProblem> todayProbList_m = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todayCardSubsectionList);
				List<FreqProbCurriDTO> todayProbList = entityToDto(todayProbList_m);
				log.info("\n(최근 공부한 소단원이 존재할 때,)오늘 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
				
				//오늘 소단원
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,1));
				
				
				//오늘 소단원에서 출제할 문제는, 최근 공부 소단원에서 출제할 문제와 중복되면 안됨.
				int todayProvidedProbId=0;
				if(todayProbList.size()!=0) {
					todayProvidedProbId = recommendFreqProbIdList.get(0).getProblemId();
				}
				List<Integer> forNotProvided = new ArrayList<Integer>();
				forNotProvided.addAll(solvedProbIdList);
				forNotProvided.add(todayProvidedProbId);

				//
				List<UserFrequentProblem> recentProbList_m = UserFreqProbRepo.getFrequentNotProvidedProblem(forNotProvided,subsectionList);
				List<FreqProbCurriDTO> recentProbList = entityToDto(recentProbList_m);
				log.info("\n최근 공부한 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + recentProbList);
				
				List<UserFrequentProblem> providedRecentProbList_m = UserFreqProbRepo.getFrequentProvidedProblem(solvedProbIdList,subsectionList);
				List<FreqProbCurriDTO> providedRecentProbList = entityToDto(providedRecentProbList_m);
				log.info("\n최근 공부한 소단원에 대한 출제한 적 있는 빈출 문제 리스트 : " + providedRecentProbList);
				
				
				//최근소단원 - 출제X
				recommendFreqProbIdList.addAll(SortingAndRecommend(recentProbList,subsectionList,2));
				//최근소단원 - 출제O
				recommendFreqProbIdList.addAll(SortingAndRecommend(providedRecentProbList,subsectionList,2));
				
				
				//최소 3문제를 확보하지 못한다면 과거 14일 기준 가장 예전에 배운 소단원보다 더 오래된 소단원들에서 빈출문제 선정 
				if(recommendFreqProbIdList.size()<3) {
					
					//
					for(int i= 0; i<recommendFreqProbIdList.size();i++) {
						forNotProvided.add(recommendFreqProbIdList.get(i).getProblemId());
					}
					
					List<String> AnothersubsectionList = new ArrayList<String>();
					AnothersubsectionList.addAll(getAnotherSubsectionMasteryOfUser(userId,subsectionList));
					
					//
					List<UserFrequentProblem> probList_m = UserFreqProbRepo.getFrequentNotProvidedProblem(forNotProvided,AnothersubsectionList);
					List<FreqProbCurriDTO> probList = entityToDto(probList_m);
					recommendFreqProbIdList.addAll(SortingAndRecommend(probList,AnothersubsectionList,3-recommendFreqProbIdList.size()));
					
					if(recommendFreqProbIdList.size()<3) {
						List<UserFrequentProblem> providedProbList_m = UserFreqProbRepo.getFrequentProvidedProblem(solvedProbIdList,AnothersubsectionList);
						List<FreqProbCurriDTO> providedProbList = entityToDto(providedProbList_m);
						recommendFreqProbIdList.addAll(SortingAndRecommend(providedProbList,AnothersubsectionList,3-recommendFreqProbIdList.size()));
					}
				}
			}
			
			//최근 공부한 소단원이 없다면 --> 오늘의학습카드 내 소단원에서 문제 개수 채우기
			else{
				
				List<UserFrequentProblem> todayProbList_m = UserFreqProbRepo.getFrequentNotProvidedProblem(solvedProbIdList,todayCardSubsectionList);
				List<FreqProbCurriDTO> todayProbList = entityToDto(todayProbList_m);
				log.info("\n(최근 공부한 소단원이 존재하지 않을 때,)오늘 소단원에 대한 출제한 적 없는 빈출 문제 리스트 : " + todayProbList);
				
				recommendFreqProbIdList.addAll(SortingAndRecommend(todayProbList,todayCardSubsectionList,5));
			}
			
		}
		
		log.info("\n최종 추천 빈출 문제 리스트 : " + recommendFreqProbIdList);
		
		return recommendFreqProbIdList;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//각 소단원마다 빈출 문제 골고루 뽑는 로직
	public List<FrequentProblemDTO> SortingAndRecommend(List<FreqProbCurriDTO> freqProbSec, List<String> subsectionList , int num) {
		
		//log.info("\n빈출문제 - 소단원 매핑 set : " + freqProbSec);
		//log.info("\n이해도 낮은 순서로 정렬된 소단원 리스트 : " + subsectionList);
		
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
		//log.info("\n소단원 이해도 낮은 순서로 골고루 빈출 문제 뽑힐 수 있게 sorting : " + resultList);
		

		//추천 문제 개수 최대 num개
		for(int i = 0 ; i<num ; i++) {
			
			if(resultList.size()==i)
				break;

			FrequentProblemDTO freqProb = new FrequentProblemDTO();
			freqProb.setProblemId(resultList.get(i).getProblemId());
			recommendFreqProbIdList.add(freqProb);
			
		}
		log.info("\n추천하고자 하는 빈출 문제 리스트 : " + recommendFreqProbIdList);
		
			
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
		//subsectionList.sort(Comparator.reverseOrder());
		
		
		
		//타겟시험범위시작 ~ 최근(14일)동안 공부했던 가장 오래전 소단원   내의 소단원과 평균 이해도 ==> 이해도 낮은 소단원에 대한 빈출문제 출제할 것
		List<UserSubSectionMastery> AnotherSubsectionList = UserSubSecMasteryRepo.getAnotherSubSectionAndMastery(start,end,userId,subsectionList);
		log.info("\n타겟시험범위시작 ~ 최근(14일)동안 공부했던 가장 오래전 소단원 -> 소단원과 평균 이해도 리스트 : " + AnotherSubsectionList);
		
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
	public List<SubsectionMasteryDTO> getSectionMasteryOfUser(String userId, List<FrequentProblemDTO> probIdList){
		
		List<SubsectionMasteryDTO> sectionMasteryList = new ArrayList<SubsectionMasteryDTO>();
		
		List<Integer> probList = new ArrayList<Integer>();
		for(int i =0; i<probIdList.size();i++) {
			probList.add(probIdList.get(i).getProblemId());
		}
		
		List<UserSectionMastery> userSectionMasteryList = UserSectionMasteryRepo.getSubSectionAndMastery(userId, probList);
		for(int i = 0 ; i<userSectionMasteryList.size() ; i++) {
			SubsectionMasteryDTO sectionMastery = new SubsectionMasteryDTO();
			sectionMastery.setSubsection(userSectionMasteryList.get(i).getSubSection());
			sectionMastery.setMastery(userSectionMasteryList.get(i).getUkMastery());
			
			sectionMasteryList.add(sectionMastery);
		}
		
		
		return sectionMasteryList;
	};
	
	
	//추천된 빈출문제 리스트를 받아 문제들의 총 예상 풀이시간 반환
	
	@Autowired
	EstimatedTimeRepo EstimatedTimeRepo;
	
	public int getestimatedTime(List<FrequentProblemDTO> probIdList){
		
		int estimatedTime = 0;
		
		List<Integer> probList = new ArrayList<Integer>();
		for(int i =0; i<probIdList.size();i++) {
			probList.add(probIdList.get(i).getProblemId());
		}
		
		List<Integer> estimatedTimeList = EstimatedTimeRepo.getEstimatedTime(probList);
		
		for(int i = 0 ; i<estimatedTimeList.size() ; i++) {
			
			if(estimatedTimeList.get(i)!=0 && estimatedTimeList!=null)
				estimatedTime += estimatedTimeList.get(i);
			else
				estimatedTime += 180;
			
		}
		
		
		return estimatedTime;
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
		

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String today = LocalDate.now().format(formatter);
		String tomorrow = LocalDate.now().plusDays(1).format(formatter);
		String Fromday = LocalDate.now().minusDays(14).format(formatter);
		
		List<String> sourceTypeListDiagnosis = new ArrayList<String>(
				Arrays.asList("diagnosis")); // 진단고사 문제
		List<String> sourceTypeListTodayCards = new ArrayList<String>(
				Arrays.asList("type_question","supple_question","section_test_question","chapter_test_question","addtl_supple_question","section_exam_question","full_scope_exam_question","trial_exam_question")); // 진단고사, 추가학습(빈출문제카드)에서 푼 문제를 제외 - 오늘의 학습 카드 source type
		List<String> sourceTypeListAll = new ArrayList<String>(
				Arrays.asList("type_question","supple_question","section_test_question","chapter_test_question","addtl_supple_question","section_exam_question","full_scope_exam_question","trial_exam_question","diagnosis","frequent_question")); // 모든 source type

		try {
			diagnosisProbIdList.addAll(this.getLRSProblemIdList(userId, null, null, sourceTypeListDiagnosis)); 
			log.info("\n진단고사에서 풀었던 probId 리스트 : " + diagnosisProbIdList);
			diagnosisSubsectionList = getSubsectionMasteryOfUser(userId, isFirstFreq, diagnosisProbIdList);
			
			todayCardProbIdList.addAll(this.getLRSProblemIdList(userId, tomorrow, today, sourceTypeListTodayCards));
			log.info("\n오늘의 학습 카드에서 풀었던 probId 리스트 : " + todayCardProbIdList);
			todayCardSubsectionList = getSubsectionMasteryOfUser(userId, isFirstFreq, todayCardProbIdList);
			
			recentSolvedProbIdList.addAll(this.getLRSProblemIdList(userId, today, Fromday, sourceTypeListTodayCards));
			log.info("\n과거 14일동안 풀었던 probId 리스트 : " + recentSolvedProbIdList);
			recentSectionList = getSubsectionMasteryOfUser(userId, isFirstFreq, recentSolvedProbIdList);
			
			solvedProbIdList.addAll(this.getLRSProblemIdList(userId, today, null, sourceTypeListAll)); 
			log.info("\n그동안 풀었던 probId 리스트 : " + solvedProbIdList);
			
			
			//다른건 문제리스트가 없을 수 있어도, 오늘의 카드 문제리스트가 없을 수는 없음
			if(!isFirstFreq&&todayCardProbIdList.size()==0) {
				FrequentCard.setResultMessage("No today-card problems in LRS.");
				return FrequentCard;
			}

				
			} catch (Exception e) {
				FrequentCard.setResultMessage(e.getMessage());
				return FrequentCard;
			}
			
			
			
		//추천할 빈출 문제
		List<FrequentProblemDTO> recommendFreqProbList = new ArrayList<FrequentProblemDTO>();
		try {
			
			recommendFreqProbList = getSubsectionFreqProb(userId, isFirstFreq, diagnosisSubsectionList, recentSectionList, todayCardSubsectionList, solvedProbIdList);
			if(recommendFreqProbList.size()!=0) {
				FrequentCard.setResultMessage("Successfully return frequent card.");
				
			}
		}
		catch (Exception e) {
			FrequentCard.setResultMessage(e.getMessage());
			return FrequentCard;
		}
		
		
		
		//빈출카드 내 문제들에 해당하는 중단원들과 이해도
		List<SubsectionMasteryDTO> sectionMasteryList = new ArrayList<SubsectionMasteryDTO>();
		sectionMasteryList = getSectionMasteryOfUser(userId, recommendFreqProbList);
		
		//빈출카드 내 문제들의 총 예상 풀이시간
		
		
		
		//최종 빈출 카드 구성
		FrequentCard.setCardType("빈출카드");
		FrequentCard.setEstimatedTime(getestimatedTime(recommendFreqProbList));
		FrequentCard.setProbSetList(recommendFreqProbList);
		FrequentCard.setSubsectionSetList(sectionMasteryList);
		return FrequentCard;
	}

}
