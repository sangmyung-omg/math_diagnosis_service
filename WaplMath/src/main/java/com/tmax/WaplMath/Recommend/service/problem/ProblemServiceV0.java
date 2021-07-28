package com.tmax.WaplMath.Recommend.service.problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.problem.DiagnosisProblem;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.model.user.UserExamScope;
import com.tmax.WaplMath.Common.repository.user.UserExamScopeRepo;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepo;
import com.tmax.WaplMath.Recommend.repository.DiagnosisProblemRepo;
import com.tmax.WaplMath.Recommend.service.userinfo.UserInfoServiceV0;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service("ProblemServiceV0")
@Primary
public class ProblemServiceV0 implements ProblemServiceBase {
	
	@Autowired
	@Qualifier("UserInfoServiceV0")
	UserInfoServiceV0 userService;
	
	@Autowired
  @Qualifier("RE-DiagnosisProblemRepo")
	DiagnosisProblemRepo diagnosisProblemRepository;
	
	@Autowired
  @Qualifier("RE-CurriculumRepo")
	CurriculumRepo curriculumRepository;
	
	@Autowired
	UserExamScopeRepo userExamScopeRepo;
	
	@Override
	public Map<String, Object> getDiagnosisProblems(String userId, String diagType){
		
		if (diagType.equalsIgnoreCase("in-depth")) {
			diagType = "꼼꼼";
			log.info("> IN-DEPTH DIAGNOSIS SERVICE START!");
		} else if (diagType.equalsIgnoreCase("simple")) {
			diagType = "간단";
			log.info("> SIMPLE DIAGNOSIS SERVICE START!");
		} else {
			log.info("error) diagType is ambiguous : " + diagType);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("error", "diagType is ambiguous : " + diagType);
			return map;
		}
		
//		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<List<Integer>> diagnosisProblems = new ArrayList<List<Integer>>();
//		List<String> partNames = new ArrayList<String>();
		
		// DB에서 part 정보 불러오기 (어떤 파트가 있는지)
		log.info("Getting part list......");
		List<String> partList = curriculumRepository.findDistinctPart();
		Collections.shuffle(partList);			// 순서 섞기
		
		// User 현재 진도 단원 정보 DB에서 조회
		User dao = userService.getUserInfo(userId);
		if (dao == null || dao.getUserUuid() == null) {
			resultMap.put("error", "no userId in user table");
			return resultMap;
		}
		
		String limit_chapter = dao.getCurrentCurriculumId();
		
		List<String> errOrderList = new ArrayList<String>();
		
		for (String partName : partList) {
			// 해당하는 영역(파트)에 따른 대단원들 DB에서 불러오기
			List<Curriculum> currQueryResult = curriculumRepository.findChaptersByPart(partName);
			List<String> chapterIdList = new ArrayList<String>();
			for (Curriculum curr : currQueryResult){
				chapterIdList.add(curr.getCurriculumId());
			}
			log.info(partName + " 에 해당하는 대단원 : " + chapterIdList.toString());

			// 진단 범위에 해당하는 대단원들 select - 현재 학기에서 2학기 전부터 현재 배우고 있는 단원 바로 이전까지 (가장 최근에 다 배운 단원 까지)
			List<String> available_chaps = new ArrayList<String>();
			
			for (String chap : chapterIdList) {					// chap : 진단 범위 후보 대단원들 , limit_chapter : 학생의 현재 진도 대단원
				int chap_grade = Integer.parseInt(chap.substring(4,5));
				int chap_semester = Integer.parseInt(chap.substring(6,7));
				int chapter_grade = Integer.parseInt(limit_chapter.substring(4,5));
				int chapter_semester = Integer.parseInt(limit_chapter.substring(6,7));
				
				// 현재 학기의 2학기 전까지의 범위 체크
				if ((chap.compareToIgnoreCase(limit_chapter) <= 0) && (2*chap_grade + chap_semester >= 2*chapter_grade + chapter_semester -2)) {
//					if (!dao.getGrade().equalsIgnoreCase("3")) {
//						available_chaps.add(chap);					
//					} else {
//						if (2*chap_grade + chap_semester < 6) {				// 3학년 dummy 문제가 없어서
//							available_chaps.add(chap);
//						}
//					}
					available_chaps.add(chap);
				}
			}
			log.info("available_chaps : " + available_chaps.toString());
			
			// available_chaps가 null이면, 각 영역에서 첫 단원 출제
			String selected_chapter = "";
			if (available_chaps.size() != 0 && available_chaps != null) {
//				Collections.shuffle(available_chaps);
				selected_chapter = available_chaps.get(available_chaps.size()-1);
				log.info("Available_chaps exist and selected : " + selected_chapter);
			} else {
				log.info("Available_chaps not exist");

				selected_chapter = chapterIdList.get(0);				
				log.info("No available chapter for the part, may because the given grade was 1, so first chapter of the part is given : " + selected_chapter);
//				if (resultMap.containsKey("error")) {
//					resultMap.replace("error", resultMap.get("error") + "\n" + "No available chapter for the part, may because the given grade was 1, so first chapter of the part is given : " + selected_chapter);
//				} else {
//					resultMap.put("error", "No available chapter for the part, may because the given grade was 1, so first chapter of the part is given : " + selected_chapter);
//				}
			}
			
			// 해당하는 단원에 대한 문제 set 가져오기
			log.info("Getting problem set...");
			DiagnosisProblem result;
			List<DiagnosisProblem> queryResult = diagnosisProblemRepository.findAllByChapter(selected_chapter, diagType);
			List<Integer> prob_list = new ArrayList<Integer>();
			
			if (queryResult.size() != 0 && queryResult != null) {
				// log.info("Available problem sets for the selected chapter : " + queryResult.toString());
				log.info("# of available problem sets for the selected chapter : " + Integer.toString(queryResult.size()) + " sets");
				Collections.shuffle(queryResult);
				result = queryResult.get(0);

				String logs = "One random problem set chosen : Set_Id " + Integer.toString(result.getDiagnosisProbId()) 
																+ " / Basic " + Integer.toString(result.getBasicProbId()) 
																+ " / Upper " + Integer.toString(result.getUpperProbId());
				if (result.getLowerProbId() != null) logs += " / Lower " + Integer.toString(result.getLowerProbId());
				log.info(logs);
			
				
				// 문제 set의 각 문제에 대한 정보 불러오기
				if (result.getLowerProbId() != null) {					
					prob_list.add(result.getLowerProbId());
				}
				prob_list.add(result.getBasicProbId());
				if (result.getUpperProbId() != null) {
					prob_list.add(result.getUpperProbId());					
				}
				
			} else {
				String order = Integer.toString(partList.indexOf(partName)+1);
				if (order.equals("1")) {
					order += "st";
				} else if (order.contentEquals("2")) {
					order += "nd";
				} else {
					order += "th";
				}
				
				errOrderList.add(order);

				// No problem set for the selected_chapter
				log.info("No ACCEPTED problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");

				// 임시 err 처리 (dummy)
				// 삭제
			}
			diagnosisProblems.add(prob_list);
		}
		
		if (errOrderList.size() != 0) {
			if (diagType.equalsIgnoreCase("in-depth")) {
				if (resultMap.containsKey("Warning")) {
					resultMap.replace("Warning", resultMap.get("Warning") + "\n" + String.join(", ", errOrderList) + " element of diagnosisProblems is dummy data due to lack of problem data in DB.");
				} else {
					resultMap.put("Warning", String.join(", ", errOrderList) + " element of diagnosisProblems is dummy data due to lack of ACCEPTED problem data in DB.");
				}								
			} else if (diagType.equalsIgnoreCase("simple")) {
				if (resultMap.containsKey("error")) {
					resultMap.replace("error", resultMap.get("error") + "\n" + String.join(", ", errOrderList) + " element of diagnosisProblems is null due to lack of ACCEPTED 'simple' problem set in DB.");
				} else {
					resultMap.put("error", String.join(", ", errOrderList) + " element of diagnosisProblems is null due to lack of ACCEPTED 'simple' problem set in DB.");
				}	
			}
		}
		
		resultMap.put("diagnosisProblems", diagnosisProblems);
//		resultMap.put("partNames", partNames);
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getExtraProblem(String userId, List<Integer> probIdList){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<Integer> extraProblems = new ArrayList<Integer>();
		log.info("> Diagnosis Extra Problem Service Start!");
		String diagType = "꼼꼼";

		// USER_MASTER 테이블에서 현재까지 배운 단원 정보 조회 & USER_EXAM_SCOPE 테이블에서 다음 시험의 범위 (단원) 조회
		log.info("Getting user info......");
		UserExamScope examScope = userExamScopeRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		String start_sub_section = examScope.getStartSubSectionId();
		String end_sub_section = examScope.getEndSubSectionId();
		String current_chapter = examScope.getUser().getCurrentCurriculumId();
		log.info(userId + "'s Sub section range for the next exam : " + start_sub_section + " ~ " + end_sub_section);
		
		// 학생의 시험 범위에 해당하는 파트 (내용영역) 조회
		log.info("Selecting target parts for the next exam......");
		List<String> partList = curriculumRepository.findDistinctPartBetween(start_sub_section, end_sub_section);
		log.info("Parts for next exam : " + partList.toString());
		
		// 시험 범위 파트에 해당하는 대단원들 조회
		Map<String, List<String>> partChapterList = new HashMap<String, List<String>>();
		for (String part : partList) {
			log.info("Getting chapters of part : " + part);
			List<Curriculum> chapters = curriculumRepository.findChaptersByPart(part);
			for (Curriculum chapter : chapters) {
				// 현재까지 배운 소단원까지만 keep.
				if (chapter.getCurriculumId().equalsIgnoreCase(current_chapter.substring(0, 11)) ||
						chapter.getCurriculumId().compareToIgnoreCase(current_chapter.substring(0, 11)) < 0) {
					if (partChapterList.containsKey(chapter.getPart())) {
						partChapterList.get(chapter.getPart()).add(chapter.getCurriculumId());
					} else {
						partChapterList.put(chapter.getPart(), new ArrayList<String>(Arrays.asList(chapter.getCurriculumId())));
					}
				}
			}
		}
		log.info("partChapterList : " + partChapterList.toString());
		
		
		// 문제 찾을 대단원 선정 : (우선순위 1) 현재 학년의 대단원들 -> (우선순위 2) 없으면, 이전 학년 대단원들
		String grade = examScope.getUser().getGrade();
		int grade_num = 0;
		Map<String, List<String>> available_chapters = new HashMap<String, List<String>>();
		for (int i=0; i<partList.size(); i++) {
			List<String> candidate_chapters = new ArrayList<String>();
			
			grade_num = Integer.parseInt(grade);
			while ((candidate_chapters.size() == 0 || candidate_chapters == null) && grade_num > 0) {
				for (String chapter : partChapterList.get(partList.get(i))) {
					if (Integer.parseInt(chapter.substring(4, 5)) == grade_num) {
						log.info(chapter);
						candidate_chapters.add(chapter);
					}
				}
				grade_num--;
			}
			
			if (candidate_chapters != null && candidate_chapters.size() != 0) {				// 이번 학년에 해당하는 단원 없으면, 파트 제외
				available_chapters.put(partList.get(i), candidate_chapters);				
			}
		}
		log.info("available_chapters : " + available_chapters.toString());
		
		if (available_chapters.size() == 0 || available_chapters == null) {
			// 에러 처리
			
		}
		
		// 파트별 문제 개수 결정 (임의로 : 시험 범위에 파트 2~3개 고정, 범위에 맞는 파트 최대 3개)
		List<Integer> num_list = determine_combination(available_chapters.keySet().size());
		log.info("num_list : " + num_list.toString());
		
		Map<String, List<Integer>> partProblemMap = new HashMap<String, List<Integer>>();
		for (String key : available_chapters.keySet()) {
			partProblemMap.put(key, new ArrayList<Integer>(Arrays.asList()));
		}
		
		int idx = 0;
		for (String part : available_chapters.keySet()) {
			List<String> list = available_chapters.get(part);
			log.info("List : " + list.toString());
			
			// (한 파트에 대해) 가장 최근 대단원부터 하나씩 loop 돌며, num_list의 해당 개수만큼 쌓이면 break;
			
			// 해당 단원의 문제 조회
			// List<DiagnosisProblem> diagList = diagnosisProblemRepository.findAllByChapter(chapter_condition, diagType);
			List<DiagnosisProblem> diagList = diagnosisProblemRepository.findAllByChapterIn(list, diagType);
			// List<DiagnosisProblem> diagList = diagnosisProblemRepository.findAllByBasicProblemProblemTypeCurriculumChapterInAndBasicProblemCategory(list, diagType);
			
			if (diagList.size() == 0 || diagList == null) {
				// 예외 처리
				log.info("diagList is empty : " + Integer.toString(diagList.size()) + " rows");
			}
			
			
			int queryIdx = 0;
			while (partProblemMap.get(part).size() < num_list.get(idx)) {
				int basic = diagList.get(queryIdx).getBasicProbId();
				int upper = diagList.get(queryIdx).getUpperProbId();
				int lower = diagList.get(queryIdx).getLowerProbId();
				
				if (!probIdList.contains(basic) && !partProblemMap.get(part).contains(basic)) {
					partProblemMap.get(part).add(basic);
				}
				if (!probIdList.contains(upper) && !partProblemMap.get(part).contains(upper)) {
					partProblemMap.get(part).add(upper);
				}
				if (!probIdList.contains(lower) && !partProblemMap.get(part).contains(lower)) {
					partProblemMap.get(part).add(lower);
				}
				queryIdx++;
				if (queryIdx >= diagList.size()) {
					break;
				}
			}

			idx++;
		}
		log.info(partProblemMap.toString());
		
		int ii = 0;
		for (String part : partProblemMap.keySet()) {
			List<Integer> partProblems = partProblemMap.get(part);
			Collections.shuffle(partProblems);
			for (int j=0; j<num_list.get(ii); j++) {
				extraProblems.add(partProblems.get(j));
			}
			ii++;
		}
		
		resultMap.put("extraProblems", extraProblems);
		return resultMap;
	}
	
	private List<Integer> determine_combination(int part_num){
		List<Integer> num_list = new ArrayList<Integer>();
		if (part_num == 1) {
			num_list.add(5);
		} else if (part_num == 2) {
			num_list.add(3);
			num_list.add(2);
		} else if (part_num == 3) {
			num_list.add(2);
			num_list.add(2);
			num_list.add(1);
		}
		Collections.shuffle(num_list);
		return num_list;
	}
}
