package com.tmax.WaplMath.AdditionalLearning.service.frequentcard;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.AdditionalLearning.dto.FreqProbCurriDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentCardDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.FrequentProblemDTO;
import com.tmax.WaplMath.AdditionalLearning.dto.SubsectionMasteryDTO;
import com.tmax.WaplMath.AdditionalLearning.service.problem.ProblemServiceBaseV0;
import com.tmax.WaplMath.AdditionalLearning.service.section.SectionServiceBaseV0;
import com.tmax.WaplMath.AdditionalLearning.util.lrs.LRSProblemRecord;
import com.tmax.WaplMath.AdditionalLearning.util.user.UserExamScopeUtil;
import com.tmax.WaplMath.Common.exception.GenericInternalException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("FrequentCardServiceV1")
public class FrequentCardServiceV1 implements FrequentCardServiceBaseV1{

	
	@Autowired
	@Qualifier("AddLearn-LRSProblemRecord")
	private LRSProblemRecord lrsProbRecord;
	
	@Autowired
	@Qualifier("AddLearn-UserExamScope")
	private  UserExamScopeUtil UserExamScope;
	
	@Autowired
	@Qualifier("AddLearn-SectionService")
	private  SectionServiceBaseV0 sectionService;
	
	@Autowired
	@Qualifier("AddLearn-ProblemService")
	private  ProblemServiceBaseV0 problemService;
	
	
	
	
	
	
	@Override
	public FrequentCardDTO getFrequentCard(String userId, boolean isFirstFrequent){
		
		FrequentCardDTO frequentCard = new FrequentCardDTO();
		frequentCard.setCardType("빈출카드");

		
		//LRS 검색 위한 input 값
		String todayStart = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String todayEnd = ZonedDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String minusDays14 = ZonedDateTime.now().minusDays(14).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		List<String> sourceTypeListDiagnosis = new ArrayList<String>(
				Arrays.asList("diagnosis","diagnosis_simple"));
		List<String> sourceTypeListTodaycards = new ArrayList<String>(
				Arrays.asList("type_question","supple_question","section_test_question","chapter_test_question","addtl_supple_question","section_exam_question","full_scope_exam_question","trial_exam_question"));
		List<String> sourceTypeListAll = new ArrayList<String>(
				Arrays.asList("type_question","supple_question","section_test_question","chapter_test_question","addtl_supple_question","frequent_question")); //빈출문제일 가능성 있는 소스타입에서만

		
		//사용자 타겟 시험범위
		List<String> ExamScope = UserExamScope.getUserExamScope(userId);
		
		//전날까지 학습한 모든 문제 id 리스트
		Set<Integer> solvedProbIdList = new HashSet<Integer>();
		solvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayStart, null, sourceTypeListAll)); 
		log.info("\nThe number of solved problem by the day before  : " + solvedProbIdList.size());
		
		if(solvedProbIdList.isEmpty()) {
			solvedProbIdList.add(0); //query에러 방지
		}
		
		
		//빈출문제 추천
		//LRS에서 필요한 문제 id 리스트 불러옴
		//학습한 문제의 소단원 도출
		//소단원에 대한 빈출문제 추출
		//빈출문제 후보군에서 추천할 문제 선택
		//추천 빈출문제가 너무 적은 경우 - 범위를 늘려서
		
		List<FrequentProblemDTO> frequentProblemList = new ArrayList<FrequentProblemDTO>();
		
		/*CASE1. 진단고사 직후 첫 접근*/
		if(isFirstFrequent) {
			log.info("\nRecommend frequent card for diagnosis. User ID : " + userId);
			//진단고사에서 학습한 문제 id 리스트 
			Set<Integer> diagnosisSolvedProbIdList = new HashSet<Integer>();

			
			diagnosisSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, null, null, sourceTypeListDiagnosis)); 
			log.info("\nDiagnosis probId List  : " + diagnosisSolvedProbIdList);
			
			if(diagnosisSolvedProbIdList.isEmpty()) {
				throw new GenericInternalException("ERR-AL-003","Diagnosis data is not found in LRS.");
			}
			
			
			//진단고사에서 학습한 소단원
			List<String> diagnosisSubsectionListBefore = sectionService.getSubsectionListByProblem(userId, diagnosisSolvedProbIdList);
			
			
			//초등 소단원 제외한 진단고사에서 학습한 소단원
			List<String> diagnosisSubsectionList = new ArrayList<String>();
			
			if(!diagnosisSubsectionListBefore.isEmpty()) {
				for(int i =0 ; i<diagnosisSubsectionListBefore.size(); i++) {
					if(!diagnosisSubsectionListBefore.get(i).contains("초등")) {
						diagnosisSubsectionList.add(diagnosisSubsectionListBefore.get(i));
					}
				}
			}
			
			log.info("\nDiagnosis Subsection List  : " + diagnosisSubsectionList);
			
			
			//진단고사 전부 초등 문제인지 확인 유무
			boolean diagAllEle = false;
			if(diagnosisSubsectionList.isEmpty()&&diagnosisSubsectionListBefore.size()>0) {
				diagAllEle = true;
			}

			
			/*CASE1-1. 진단고사에서 학습한 소단원이 있다면*/
			if(!diagnosisSubsectionList.isEmpty()) {
				//진단고사에서 학습한 소단원의 출제한 적 없는 빈출문제
				List<FreqProbCurriDTO> diagnosisFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdList, diagnosisSubsectionList);
				
				if(diagnosisFreqProbCurriList.isEmpty()) {
					throw new GenericInternalException("ERR-AL-003","There is no frequent problem in learned diagnosis subsection.");
				}
				log.info("\nDiagnosis Subsection with frequent problem  : " + getSubsecListWithFreqProb(diagnosisFreqProbCurriList));
				log.info("\nFrequent problem count in Diagnosis Subsection : " + diagnosisFreqProbCurriList.size());

				
				//빈출문제 추천
				List<FrequentProblemDTO> diagnosisRecommendFreqProbList = problemService.SortingAndRecommend(diagnosisFreqProbCurriList, diagnosisSubsectionList, 5);
				frequentProblemList.addAll(diagnosisRecommendFreqProbList);
			}
			
			/*CASE1-2. 진단고사에서 학습한 소단원이 없다면 - 전부 초등소단원일 경우 & exception*/
			else {
				
				if(diagAllEle) {
					
					log.info("\nAll diagnosis problems are elementary curriculum.");
					//오늘의학습카드에서 학습한 문제 id 리스트
					Set<Integer> todaycardSolvedProbIdList = new HashSet<Integer>();
					
					todaycardSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayEnd, todayStart, sourceTypeListTodaycards)); 
					log.info("\nTodaycards probId List  : " + todaycardSolvedProbIdList);
					
					
					if(todaycardSolvedProbIdList.isEmpty()) {
						throw new GenericInternalException("ERR-AL-003","Todaycards data is not found in LRS.");
					}
					
					//오늘의학습카드에서 학습한 소단원
					List<String> todaycardSubsectionList = sectionService.getSubsectionListByProblem(userId, todaycardSolvedProbIdList);
					log.info("\nTodaycards Subsection List  : " + todaycardSubsectionList);
					
					//오늘의학습카드에서 학습한 소단원의 출제한 적 없는 빈출문제
					List<FreqProbCurriDTO> todaycardFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdList, todaycardSubsectionList);

					
					if(todaycardFreqProbCurriList.isEmpty()) {
						throw new GenericInternalException("ERR-AL-003","There is no frequent problem in learned todaycards subsection.");
					}
					
					log.info("\nTodaycards Subsection with frequent problem  : " + getSubsecListWithFreqProb(todaycardFreqProbCurriList));
					log.info("\nFrequent problem count in Todaycards Subsection : " + todaycardFreqProbCurriList.size());
					
					
					//빈출문제 추천
					List<FrequentProblemDTO> todayRecommendFreqProbList = problemService.SortingAndRecommend(todaycardFreqProbCurriList, todaycardSubsectionList, 5);
					
					frequentProblemList.addAll(todayRecommendFreqProbList);
				}
				else {
					throw new GenericInternalException("ERR-AL-003","The problem exists, but the subsection does not exist. Check user mastery. User ID = "+userId);
				}
			}	
			
			if(!frequentProblemList.isEmpty()) {
				frequentCard.setResultMessage("Successfully return frequent card for Diagnosis.");
			}
		}
		
		/*CASE2. 일반적인 접근*/
		else {
			
			log.info("\nRecommend frequent card - Learned subsection. User ID : " + userId);
			//오늘의학습카드에서 학습한 문제 id 리스트
			Set<Integer> todaycardSolvedProbIdList = new HashSet<Integer>();
			
			//최근(과거 14일) 학습한 문제 id 리스트
			Set<Integer> recentSolvedProbIdList = new HashSet<Integer>();
			

			todaycardSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayEnd, todayStart, sourceTypeListTodaycards)); 
			log.info("\nTodaycards probId List  : " + todaycardSolvedProbIdList);
			
			if(todaycardSolvedProbIdList.isEmpty()) {
				throw new GenericInternalException("ERR-AL-003","Todaycards data is not found in LRS.");
			}
			
			
			recentSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayStart, minusDays14, sourceTypeListTodaycards)); 
			log.info("\nThe number of recent 14 days probId List  : " + recentSolvedProbIdList.size());
			
			

			
			//오늘의학습카드에서 학습한 소단원
			List<String> todaycardSubsectionList = sectionService.getSubsectionListByProblem(userId, todaycardSolvedProbIdList);
			log.info("\nTodaycards Subsection List  : " + todaycardSubsectionList);
			
			//최근(과거 14일) 학습한 소단원
			List<String> recentSubsectionList = sectionService.getSubsectionListByProblemWithScope(userId, recentSolvedProbIdList, ExamScope.get(0), "중등-중4");
			log.info("\nRecent 14 days Subsection List  : " + recentSubsectionList);
			
			
			
			if((!todaycardSolvedProbIdList.isEmpty()&&todaycardSubsectionList.isEmpty())||(!recentSolvedProbIdList.isEmpty()&&recentSubsectionList.isEmpty())) {
				throw new GenericInternalException("ERR-AL-003","The problem exists, but the subsection does not exist. Check user mastery. User ID = "+userId);
			}
			
			
			

			
			
			//오늘의학습카드에서 학습한 소단원의 출제한 적 없는 빈출문제
			List<FreqProbCurriDTO> todaycardFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdList, todaycardSubsectionList);

			log.info("\nTodaycards Subsection with frequent problem  : " + getSubsecListWithFreqProb(todaycardFreqProbCurriList));
			log.info("\nFrequent problem count in Todaycards Subsection : " + todaycardFreqProbCurriList.size());
			
			List<FrequentProblemDTO> todayRecommendFreqProbList = problemService.SortingAndRecommend(todaycardFreqProbCurriList, todaycardSubsectionList, 1);
			frequentProblemList.addAll(todayRecommendFreqProbList);
			
			//오늘 소단원에서 출제할 문제는, 최근 공부 소단원에서 출제할 문제와 중복되면 안됨.
			Set<Integer> solvedProbIdListAndTodayRec = new HashSet<Integer>();
			if(!todayRecommendFreqProbList.isEmpty()) {
				solvedProbIdListAndTodayRec.add(todayRecommendFreqProbList.get(0).getProblemId());
			}
			
			//최근(과거 14일) 학습한 소단원의 출제한 적 없는 빈출문제
			List<FreqProbCurriDTO> recentFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdListAndTodayRec, recentSubsectionList);

			List<FrequentProblemDTO> recentRecommendFreqProbList = problemService.SortingAndRecommend(recentFreqProbCurriList, recentSubsectionList, 2);
			frequentProblemList.addAll(recentRecommendFreqProbList);
			
			//최근(과거 14일) 학습한 소단원의 출제한 적 있는 빈출문제
			List<FreqProbCurriDTO> providedRecentFreqProbCurriList = problemService.getProvidedFreqProbListBySubsection(solvedProbIdList, recentSubsectionList);
		
			List<FrequentProblemDTO> providedRecentRecommendFreqProbList = problemService.SortingAndRecommend(providedRecentFreqProbCurriList, recentSubsectionList, 2);
			frequentProblemList.addAll(providedRecentRecommendFreqProbList);
			
			
			//
			List<FreqProbCurriDTO> allRecentFreqProbCurriList = new ArrayList<FreqProbCurriDTO>();
			allRecentFreqProbCurriList.addAll(recentFreqProbCurriList);
			allRecentFreqProbCurriList.addAll(providedRecentFreqProbCurriList);
			
			log.info("\nRecent 14 days Subsection with frequent problem  : " + getSubsecListWithFreqProb(allRecentFreqProbCurriList));
			log.info("\nFrequent problem count in Recent 14 days Subsection : " + allRecentFreqProbCurriList.size());
			
			
	
			
			//오늘+최근 학습한 범위에서 추천된 빈출문제 개수 < 3 이면, 범위를 더 늘림(타겟시험 시작 ~ )
			int firstRecommendNum = todayRecommendFreqProbList.size()+recentRecommendFreqProbList.size()+providedRecentRecommendFreqProbList.size();
			
			if(firstRecommendNum<3) {
				log.info("\n Recommend frequent problem count < 3");
				Set<Integer> recommendList = new HashSet<Integer>();
				if(firstRecommendNum!=0) {
					for(FrequentProblemDTO dto :frequentProblemList) {
						int probId = dto.getProblemId();
						recommendList.add(probId);
					}
				}else {
					recommendList.add(0);
				}
				
				recentSubsectionList.sort(null);
				List<String> AnotherSubsectionList = sectionService.getSubsectionListByCurriScope(userId, ExamScope.get(0), recentSubsectionList.get(0));
				List<FreqProbCurriDTO> AnotherFreqProbCurriList = problemService.getAllFreqProbListBySubsection(recommendList,AnotherSubsectionList);
				List<FrequentProblemDTO> AnotherRecommendFreqProbList = problemService.SortingAndRecommend(AnotherFreqProbCurriList, AnotherSubsectionList, 3-firstRecommendNum);
				frequentProblemList.addAll(AnotherRecommendFreqProbList);
				
				log.info("\nAnother Subsection with frequent problem  : " + getSubsecListWithFreqProb(AnotherFreqProbCurriList));
				log.info("\nFrequent problem count in Another Subsection : " + AnotherFreqProbCurriList.size());
				
				

				
				if(todaycardFreqProbCurriList.size()+allRecentFreqProbCurriList.size()+AnotherFreqProbCurriList.size()==0) {
					throw new GenericInternalException("ERR-AL-003","There is no frequent problem in learned subsection.");
				}
			}
			
			if(!frequentProblemList.isEmpty()) {
				frequentCard.setResultMessage("Successfully return frequent card.");
			}
		}
		
		log.info("\nFinal frequent Problem List : " + frequentProblemList);
		frequentCard.setProbSetList(frequentProblemList);
		
		
		
		//예상풀이시간
		int estimatedTime = 0;
		for(int i = 0 ; i<frequentProblemList.size() ; i++) {

			if(frequentProblemList.get(i).getTimeRecommendation()!=null) {
				if(frequentProblemList.get(i).getTimeRecommendation()!=0)
					estimatedTime += frequentProblemList.get(i).getTimeRecommendation();
				else
					estimatedTime += 180;
			}else {
				estimatedTime += 180;
			}
		}
		
		frequentCard.setEstimatedTime(estimatedTime);

		
		
		//추천한 빈출문제 (소단원,이해도) 리스트
		List<SubsectionMasteryDTO> subsectionMasteryList = new ArrayList<SubsectionMasteryDTO>();
		Set <Integer> RecommendProbList = new HashSet<Integer>();
		for(FrequentProblemDTO dto : frequentProblemList) {
			int probId = dto.getProblemId();
			RecommendProbList.add(probId);
		}
		subsectionMasteryList = sectionService.getSubsectionMasteryListByProblem(userId, RecommendProbList);
		frequentCard.setSubsectionSetList(subsectionMasteryList);
		
		
		return frequentCard;
	}
	
	
	
	
	//(빈출문제,소단원)DTO List에서 소단원 List만 뽑기
	private Set<String> getSubsecListWithFreqProb(List<FreqProbCurriDTO> freqProbCurriDTOList){
		Set<String> curriculumIdlist = new HashSet<String>();
		for(FreqProbCurriDTO dto : freqProbCurriDTOList) {
			String str = dto.getCurriculumId();
			curriculumIdlist.add(str);
		}
		return curriculumIdlist;
	}
}
