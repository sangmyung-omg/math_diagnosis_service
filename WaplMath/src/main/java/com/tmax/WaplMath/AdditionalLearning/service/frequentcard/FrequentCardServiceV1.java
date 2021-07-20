package com.tmax.WaplMath.AdditionalLearning.service.frequentcard;

import java.time.LocalDate;
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
		
		/*LRS에서 필요한 문제 id 리스트 불러옴*/
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String todayStart = LocalDate.now().format(formatter); //오늘 0시
		String todayEnd = LocalDate.now().plusDays(1).format(formatter); //오늘 24시
		String minusDays14 = LocalDate.now().minusDays(14).format(formatter);

		List<String> sourceTypeListDiagnosis = new ArrayList<String>(
				Arrays.asList("diagnosis"));
		List<String> sourceTypeListTodaycards = new ArrayList<String>(
				Arrays.asList("type_question","supple_question","section_test_question","chapter_test_question","addtl_supple_question","section_exam_question","full_scope_exam_question","trial_exam_question"));
		List<String> sourceTypeListAll = new ArrayList<String>(
				Arrays.asList("type_question","supple_question","section_test_question","chapter_test_question","addtl_supple_question","section_exam_question","full_scope_exam_question","trial_exam_question","diagnosis","frequent_question"));

		
		//진단고사에서 학습한 문제 id 리스트 
		Set<Integer> diagnosisSolvedProbIdList = new HashSet<Integer>();

		//오늘의학습카드에서 학습한 문제 id 리스트
		Set<Integer> todaycardSolvedProbIdList = new HashSet<Integer>();
		
		//최근(과거 14일) 학습한 문제 id 리스트
		Set<Integer> recentSolvedProbIdList = new HashSet<Integer>();
		
		//전날까지 학습한 모든 문제 id 리스트
		Set<Integer> solvedProbIdList = new HashSet<Integer>();
		

		
		todaycardSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayEnd, todayStart, sourceTypeListTodaycards)); 
		log.info("\n오늘의학습카드에서 학습한 문제 id 리스트  : " + todaycardSolvedProbIdList);
		
//		if(todaycardSolvedProbIdList.isEmpty()) {
//			throw new GenericInternalException("ERR-AL-003","Todaycards data is not found in LRS.");
//		}
		

		diagnosisSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, null, null, sourceTypeListDiagnosis)); 
		log.info("\n진단고사에서 학습한 문제 id 리스트  : " + diagnosisSolvedProbIdList);
		
		if(diagnosisSolvedProbIdList.isEmpty()) {
			throw new GenericInternalException("ERR-AL-003","Diagnosis data is not found in LRS.");
		}
		
		recentSolvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayStart, minusDays14, sourceTypeListTodaycards)); 
		log.info("\n최근(과거 14일) 학습한 문제 id 리스트  : " + recentSolvedProbIdList);
		
		solvedProbIdList.addAll(lrsProbRecord.getLRSProblemIdList(userId, todayStart, null, sourceTypeListAll)); 
		log.info("\n전날까지 학습한 모든 문제 id 리스트  : " + solvedProbIdList);
		
		
		
		
		/*학습한 문제의 소단원 도출*/
		
		//사용자 타겟 시험범위
		List<String> ExamScope = UserExamScope.getUserExamScope(userId);
		
		//오늘의학습카드에서 학습한 소단원
		List<String> todaycardSubsectionList = sectionService.getSubsectionListByProblem(userId, todaycardSolvedProbIdList, "중등-중0", "중등-중4");
		log.info("\n오늘의학습카드에서 학습한 소단원(이해도 낮은 순)  : " + todaycardSubsectionList);
		

		//진단고사에서 학습한 소단원
		List<String> diagnosisSubsectionList = sectionService.getSubsectionListByProblem(userId, diagnosisSolvedProbIdList, "중등-중0", "중등-중4");
		log.info("\n진단고사에서 학습한 소단원(이해도 낮은 순)  : " + diagnosisSubsectionList);
		

		//최근(과거 14일) 학습한 소단원
		List<String> recentSubsectionList = sectionService.getSubsectionListByProblem(userId, recentSolvedProbIdList, ExamScope.get(0), "중등-중4");
		log.info("\n최근(과거 14일) 학습한 소단원(이해도 낮은 순)  : " + recentSubsectionList);

		
		
		/*소단원에 대한 빈출문제 추천*/
		
		//오늘의학습카드에서 학습한 소단원의 출제한 적 없는 빈출문제
		List<FreqProbCurriDTO> todaycardFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdList, todaycardSubsectionList);
		log.info("\n오늘의학습카드에서 학습한 소단원의 출제한 적 없는 빈출문제  : " + todaycardFreqProbCurriList);
		
		//진단고사에서 학습한 소단원의 출제한 적 없는 빈출문제
		List<FreqProbCurriDTO> diagnosisFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdList, diagnosisSubsectionList);
		log.info("\n진단고사에서 학습한 소단원의 출제한 적 없는 빈출문제  : " + diagnosisFreqProbCurriList);
		
		//최근(과거 14일) 학습한 소단원의 출제한 적 없는 빈출문제
		List<FreqProbCurriDTO> recentFreqProbCurriList = problemService.getNotProvidedFreqProbListBySubsection(solvedProbIdList, recentSubsectionList);
		log.info("\n최근(과거 14일) 학습한 소단원의 출제한 적 없는 빈출문제  : " + recentFreqProbCurriList);
		
		
		return null;
	}
}
