package com.tmax.WaplMath.Recommend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.common.DemoChapterPartMapper;
import com.tmax.WaplMath.Recommend.model.Curriculum;
import com.tmax.WaplMath.Recommend.model.Problem;
import com.tmax.WaplMath.Recommend.model.DiagnosisProblem;
import com.tmax.WaplMath.Recommend.model.User;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepository;
import com.tmax.WaplMath.Recommend.repository.DiagnosisProblemRepository;


@Service
public class ProblemService {
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	UserInfoService userService;
	
	@Autowired
	DiagnosisProblemRepository diagnosisProblemRepository;
	
	@Autowired
	ProblemRepository problemRepository;
	
	@Autowired
	CurriculumRepository curriculumRepository;
	
	
	public Map<String, Object> getNextProblemSet(String userId, String diagType, String part){

//		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		
		// part 코드를 실제 이름으로 변환
		String partName = "";
		if (part.equalsIgnoreCase("0")) partName = "수와 연산";
		else if (part.equalsIgnoreCase("1")) partName = "문자와 식";
		else if (part.equalsIgnoreCase("2")) partName = "함수";
		else if (part.equalsIgnoreCase("3")) partName = "기하";
		else if (part.equalsIgnoreCase("4")) partName = "확률과 통계";
		else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("error", "Invalid value for part(0 ~ 4) : " + part);
			return map;
		}
			
		
		// 해당하는 영역(파트)에 따른 대단원들 DB에서 불러오기
		List<Curriculum> currQueryResult = curriculumRepository.findChaptersByParts(partName);
		Map<String, List<String>> chapterIdList = new HashMap<String, List<String>>();
		for (Curriculum curr : currQueryResult){
			if (chapterIdList.containsKey(curr.getPart())) {
//				logger.info("if : " + partName + ", " + curr.getCurriculumId());
				chapterIdList.get(curr.getPart()).add(curr.getCurriculumId());
			} else {
				List<String> temp = new ArrayList<String>();
//				logger.info("else : " + partName + ", " + curr.getCurriculumId());
				temp.add(curr.getCurriculumId());
				chapterIdList.put(curr.getPart(), temp);
			}
		}
		logger.info(chapterIdList.toString());
		
		
//		// 일단은 static하게 dummy 리스트 가지고 있는 것 불러오기
//		DemoChapterPartMapper chapterPartMapper = new DemoChapterPartMapper();
//		Map<String, List<String>> chapterIdList = chapterPartMapper.getMappingInfo();
//		
//		logger.info(chapterIdList.toString());
//		System.out.println(chapterIdList);
//		
//		System.out.println("containsKey : " + chapterIdList.containsKey(part));
		
		// User 현재 진도 단원 정보 DB에서 조회
		User dao = userService.getUserInfo(userId);
//		logger.info("dao : " + dao);
		if (dao == null || dao.getUserUuid() == null) {
			Map<String, Object> err = new HashMap<String, Object>();
			err.put("error", "no userId in user table");
			return err;
		}
		String chapter = dao.getCurrentCurriculumId();
		
		// 진단 범위에 해당하는 대단원들 select - 현재 학기에서 2학기 전부터 현재 배우고 있는 단원 바로 이전까지 (가장 최근에 다 배운 단원 까지)
		List<String> available_chaps = new ArrayList<String>();
		
		for (String chap : chapterIdList.get(partName)) {					// chap : 진단 범위 후보 대단원들 , chapter : 학생의 현재 진도 대단원
			int chap_grade = Integer.parseInt(chap.substring(4,5));
			int chap_semester = Integer.parseInt(chap.substring(6,7));
			int chapter_grade = Integer.parseInt(chapter.substring(4,5));
			int chapter_semester = Integer.parseInt(chapter.substring(6,7));
			
			// 현재 학기의 2학기 전까지의 범위 체크
			if ((chap.compareToIgnoreCase(chapter) < 0) && (2*chap_grade + chap_semester >= 2*chapter_grade + chapter_semester -2)) {
				if (!dao.getGrade().equalsIgnoreCase("3")) {
					available_chaps.add(chap);					
				} else {
					if (2*chap_grade + chap_semester < 6) {				// 3학년 dummy 문제가 없어서
						available_chaps.add(chap);
					}
				}
			}
		}
		
		// available_chaps가 null이면, 각 영역에서 첫 단원 출제
		String selected_chapter = "";
		if (available_chaps.size() != 0 && available_chaps != null) {
			Collections.shuffle(available_chaps);
			selected_chapter = available_chaps.get(0);
			logger.info("Available_chaps exist and selected : " + selected_chapter);
		} else {
			logger.info("Available_chaps not exist");
			if (dao.getGrade() == "3") {
				List<String> imsi = new ArrayList<String>();
				for (String c : chapterIdList.get(partName)) {
					if (c.compareToIgnoreCase("중등-중2-2학") < 0) {
						imsi.add(c);
//						logger.info("imsi : " + imsi);
					}
				}
				Collections.shuffle(imsi);
				selected_chapter = imsi.get(0);
				logger.info("No dummy problem set for 3rd grade ㅠㅠ, so random problem set returned : " + selected_chapter);
			} else {
				selected_chapter = chapterIdList.get(partName).get(0);				
				logger.info("No available chapter for the part, so first chapter of the part is given : " + selected_chapter);
			}
		}
		
		// 해당하는 단원에 대한 문제 set 가져오기
		logger.info("Getting problem set...");
		DiagnosisProblem result;
		List<DiagnosisProblem> queryResult = diagnosisProblemRepository.findAllByChapter(selected_chapter);
		if (queryResult.size() != 0 && queryResult != null) {
			logger.info("Available problem sets for the selected chapter : " + queryResult.toString());
			Collections.shuffle(queryResult);
			result = queryResult.get(0);
		} else {
			// No problem set for the selected_chapter
			logger.info("No problem set found for the selected_chapter : " + selected_chapter);
			Map<String, Object> err = new HashMap<String, Object>();
			err.put("error", "No problem set found for the selected_chapter : " + selected_chapter);
			return err;
		}
		
		// 문제 set의 각 문제에 대한 정보 불러오기
		List<Integer> prob_list = new ArrayList<Integer>();
		prob_list.add(result.getLowerProbId());
		prob_list.add(result.getBasicProbId());
		prob_list.add(result.getUpperProbId());
		
		Map<String, Object> probIdList = new HashMap<String, Object>();
		probIdList.put("probIdList", prob_list);
		return probIdList;
	}
}
