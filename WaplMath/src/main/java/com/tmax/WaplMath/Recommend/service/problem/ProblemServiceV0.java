package com.tmax.WaplMath.Recommend.service.problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Problem.repository.ProblemRepository;
import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.problem.DiagnosisProblem;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.DiagnosisProblemRepository;
import com.tmax.WaplMath.Recommend.service.userinfo.UserInfoServiceV0;


@Service("ProblemServiceV0")
@Primary
public class ProblemServiceV0 implements ProblemServiceBase {
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	@Qualifier("UserInfoServiceV0")
	UserInfoServiceV0 userService;
	
	@Autowired
	DiagnosisProblemRepository diagnosisProblemRepository;
	
	@Autowired
	ProblemRepository problemRepository;
	
	@Autowired
	CurriculumRepository curriculumRepository;
	
	@Override
	public Map<String, Object> getDiagnosisProblems(String userId, String diagType){

//		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<List<Integer>> diagnosisProblems = new ArrayList<List<Integer>>();
//		List<String> partNames = new ArrayList<String>();
		
		// DB에서 part 정보 불러오기 (어떤 파트가 있는지)
		logger.info("Getting part list......");
		List<String> partList = curriculumRepository.findDistinctPart();
		Collections.shuffle(partList);			// 순서 섞기
		
		// User 현재 진도 단원 정보 DB에서 조회
		User dao = userService.getUserInfo(userId);
//				logger.info("dao : " + dao);
		if (dao == null || dao.getUserUuid() == null) {
			resultMap.put("error", "no userId in user table");
			return resultMap;
		}
		
		String limit_chapter = dao.getCurrentCurriculumId();
		
		List<String> errOrderList = new ArrayList<String>();
		
		for (String partName : partList) {
			// 해당하는 영역(파트)에 따른 대단원들 DB에서 불러오기
			List<Curriculum> currQueryResult = curriculumRepository.findChaptersByPart(partName);
//			Map<String, List<String>> chapterIdList = new HashMap<String, List<String>>();
			List<String> chapterIdList = new ArrayList<String>();
			for (Curriculum curr : currQueryResult){
				chapterIdList.add(curr.getCurriculumId());
			}
			logger.info(partName + " 에 해당하는 대단원 : " + chapterIdList.toString());

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
			logger.info("available_chaps : " + available_chaps.toString());
			
			// available_chaps가 null이면, 각 영역에서 첫 단원 출제
			String selected_chapter = "";
			if (available_chaps.size() != 0 && available_chaps != null) {
//				Collections.shuffle(available_chaps);
				selected_chapter = available_chaps.get(available_chaps.size()-1);
				logger.info("Available_chaps exist and selected : " + selected_chapter);
			} else {
				logger.info("Available_chaps not exist");

				selected_chapter = chapterIdList.get(0);				
				logger.info("No available chapter for the part, may because the given grade was 1, so first chapter of the part is given : " + selected_chapter);
//				if (resultMap.containsKey("error")) {
//					resultMap.replace("error", resultMap.get("error") + "\n" + "No available chapter for the part, may because the given grade was 1, so first chapter of the part is given : " + selected_chapter);
//				} else {
//					resultMap.put("error", "No available chapter for the part, may because the given grade was 1, so first chapter of the part is given : " + selected_chapter);
//				}
			}
			
			// 해당하는 단원에 대한 문제 set 가져오기
			logger.info("Getting problem set...");
			DiagnosisProblem result;
			List<DiagnosisProblem> queryResult = diagnosisProblemRepository.findAllByChapter(selected_chapter);
			List<Integer> prob_list = new ArrayList<Integer>();
			
			if (queryResult.size() != 0 && queryResult != null) {
				logger.info("Available problem sets for the selected chapter : " + queryResult.toString());
				Collections.shuffle(queryResult);
				result = queryResult.get(0);
				
				// 문제 set의 각 문제에 대한 정보 불러오기
				prob_list.add(result.getLowerProbId());
				prob_list.add(result.getBasicProbId());
				prob_list.add(result.getUpperProbId());
				
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
				logger.info("No ACCEPTED problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");
//				if (resultMap.containsKey("error")) {
//					resultMap.replace("error", resultMap.get("error") + "\n" + "No problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");
//				} else {
//					resultMap.put("error", order + " element of diagnosisProblems isNo problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");
//				}
			
				// 임시 err 처리 (dummy)
				if (partName.equalsIgnoreCase("기하")) {
					prob_list.add(7363);
					prob_list.add(7361);
					prob_list.add(7362);
				} else if (partName.equalsIgnoreCase("확률과 통계")) {
					prob_list.add(7352);
					prob_list.add(7350);
					prob_list.add(7351);					
				} else if (partName.equalsIgnoreCase("함수")) {
					prob_list.add(4505);
					prob_list.add(5081);
					prob_list.add(6595);					
				} else if (partName.equalsIgnoreCase("문자와 식")) {
					prob_list.add(4562);
					prob_list.add(4594);
					prob_list.add(4575);					
				} else if (partName.equalsIgnoreCase("수와 연산")) {
					prob_list.add(277);
					prob_list.add(308);
					prob_list.add(424);					
				}
				
			}
			diagnosisProblems.add(prob_list);
		}
		
		if (errOrderList.size() != 0) {
			if (resultMap.containsKey("Warning")) {
				resultMap.replace("Warning", resultMap.get("error") + "\n" + String.join(", ", errOrderList) + " element of diagnosisProblems is dummy data due to lack of problem data in DB.");
			} else {
				resultMap.put("Warning", String.join(", ", errOrderList) + " element of diagnosisProblems is dummy data due to lack of ACCEPTED problem data in DB.");
			}				
		}
		
		resultMap.put("diagnosisProblems", diagnosisProblems);
//		resultMap.put("partNames", partNames);
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getAdaptiveProblem(String token, String diagType, Integer probId){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		User dao = userService.getUserInfo(token);
//		logger.info("dao : " + dao);
		if (dao == null || dao.getUserUuid() == null) {
			resultMap.put("error", "no user token in user table");
			return resultMap;
		}
		String limit_chapter = dao.getCurrentCurriculumId();
		
		// DB에서 해당 문제가 어떤 part의 문제인지 불러오기
		logger.info("Getting the part of given problem......");
		String partName = "";
		String chapter = "";
		Optional<Problem> partOptional = problemRepository.findById(probId);
		if (partOptional.isPresent()) {
			partName = partOptional.get().getProblemType().getCurriculum().getPart();
			chapter = partOptional.get().getProblemType().getCurriculum().getCurriculumId();
		} else {
			logger.info("No problem for the probId : " + Integer.toString(probId));
			resultMap.put("error", "No problem for the probId : " + Integer.toString(probId));
			return resultMap;
		}
		
		// 해당하는 영역(파트)에 따른 대단원들 DB에서 불러오기
		List<Curriculum> currQueryResult = curriculumRepository.findChaptersByPart(partName);
		List<String> chapterIdList = new ArrayList<String>();
		for (Curriculum curr : currQueryResult){
			chapterIdList.add(curr.getCurriculumId());
		}
		logger.info(chapterIdList.toString());
		
		// 같은 파트에서 다음 문제 줄 만한 단원 선정
		List<String> available_chaps = new ArrayList<String>();
		for (String chap : chapterIdList) {
			System.out.println(chap + ", " + chapter + ", " + limit_chapter + " : " + (chap.compareToIgnoreCase(chapter) > 0) + ", " + (chap.compareToIgnoreCase(limit_chapter) < 0));
			if (chap.compareToIgnoreCase(chapter) > 0 && chap.compareToIgnoreCase(limit_chapter) < 0) {
				available_chaps.add(chap);
			}
		}
		
		// 푼 문제의 단원보다 크고, 학생의 현재 진도 단원보다 적은 사이 단원이 존재하면 거기서 출제, 없으면 그냥 원래 문제의 단원에서 출제
		String selected_chapter = "";
		if (available_chaps.size() == 0) {
			selected_chapter = chapter.substring(0, 11);
		} else {
			Collections.shuffle(available_chaps);
			selected_chapter = available_chaps.get(0);
		}
		System.out.println(selected_chapter);
		
		
		// 해당하는 단원에 대한 문제 set 가져오기
		logger.info("Getting problem set...");
		DiagnosisProblem result;
		List<DiagnosisProblem> queryResult = diagnosisProblemRepository.findAllByChapter(selected_chapter);
		if (queryResult.size() != 0 && queryResult != null) {
			logger.info("Available problem sets for the selected chapter : " + queryResult.toString());
			Collections.shuffle(queryResult);
			result = queryResult.get(0);
			if (result.getUpperProbId().intValue() == probId.intValue()) {
				if (queryResult.size() > 1) {
					result = queryResult.get(1);					
				} else {
					resultMap.put("error", "No other problem set in the chapter : " + selected_chapter);
				}
			}
		} else {
			// No problem set for the selected_chapter
			logger.info("No problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");
			if (resultMap.containsKey("error")) {
				resultMap.replace("error", resultMap.get("error") + "\n" + "No problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");
			} else {
				resultMap.put("error", "No problem set found for the selected_chapter : " + selected_chapter + " (part : " + partName + ")");
			}
//						partNames.add(partName);
			return resultMap;
		}
		
		// 문제 set의 난이도 '상' 문제의 ID 반환
		resultMap.put("adaptiveProblem", result.getUpperProbId());
		return resultMap;
	}
}
