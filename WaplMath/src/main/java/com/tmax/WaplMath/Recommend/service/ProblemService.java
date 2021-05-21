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
import com.tmax.WaplMath.Recommend.model.ProblemDemo;
import com.tmax.WaplMath.Recommend.model.ProblemSetDemo;
import com.tmax.WaplMath.Recommend.model.User;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemDemoRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemSetRepository;


@Service
public class ProblemService {
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	UserInfoService userService;
	
	@Autowired
	ProblemSetRepository problemSetRepository;
	
	@Autowired
	ProblemDemoRepository problemRepository;
	
	@Autowired
	CurriculumRepository curriculumRepository;
	
	
	public List<Map<String, String>> getNextProblemSet(String userId, String diagType, String part){

		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		
		// 해당하는 영역(파트)에 따른 대단원들 DB에서 불러오기
		List<Curriculum> currQueryResult = curriculumRepository.findChaptersByParts(part);
		Map<String, List<String>> chapterIdList2 = new HashMap<String, List<String>>();
		for (Curriculum curr : currQueryResult){
			if (chapterIdList2.containsKey(curr.getPart())) {
				chapterIdList2.get(curr.getPart()).add(curr.getCurriculumId());
			} else {
				List<String> temp = new ArrayList<String>();
				temp.add(curr.getCurriculumId());
				chapterIdList2.put(curr.getPart(), temp);
			}
		}
		logger.info(chapterIdList2.toString());
		
		
		// 일단은 static하게 dummy 리스트 가지고 있는 것 불러오기
		DemoChapterPartMapper chapterPartMapper = new DemoChapterPartMapper();
		Map<String, List<String>> chapterIdList = chapterPartMapper.getMappingInfo();
		
		logger.info(chapterIdList.toString());
		System.out.println(chapterIdList);
		
		System.out.println("containsKey : " + chapterIdList.containsKey(part));
		
		// User 현재 진도 단원 정보 DB에서 조회
		System.out.println(userId);
		User dao = userService.getUserInfo(userId);
		System.out.println("dao : " + dao);
		if (dao == null || dao.getUserUuid() == null) {
			Map<String, String> err = new HashMap<String, String>();
			err.put("error", "no userId in user table");
			list.add(err);
			return list;
		}
		String chapter = dao.getCurrentCurriculumId();
		List<String> available_chaps = new ArrayList<String>();
		System.out.println("part, chapter : " + part + ", " + chapter);
		
		for (String chap : chapterIdList.get(part)) {
			System.out.println(chap + ", " + chapter);
			int chap_grade = Integer.parseInt(chap.substring(4,5));
			int chap_semester = Integer.parseInt(chap.substring(6,7));
			int chapter_grade = Integer.parseInt(chapter.substring(4,5));
			int chapter_semester = Integer.parseInt(chapter.substring(6,7));
			System.out.println(Integer.toString(2*chap_grade + chap_semester) + ", " + Integer.toString(2*chapter_grade + chapter_semester -2));
			logger.info("grade : " + dao.getGrade());
			if ((chap.compareToIgnoreCase(chapter) < 0) && (2*chap_grade + chap_semester >= 2*chapter_grade + chapter_semester -2)) {
				if (!dao.getGrade().equalsIgnoreCase("3")) {
					available_chaps.add(chap);					
				} else {
					logger.info("asdfasdf");
					if (2*chap_grade + chap_semester < 6) {
						available_chaps.add(chap);
					}
				}
			}
		}
		System.out.println("available_chaps : " + available_chaps);
		
		// available_chaps가 null이면, 각 영역에서 첫 단원 출제
		String selected_chapter = "";
		if (available_chaps.size() != 0 && available_chaps != null) {
			Collections.shuffle(available_chaps);
			selected_chapter = available_chaps.get(0);
			System.out.println("Available_chaps exist and selected : " + selected_chapter);
		} else {
			System.out.println("Available_chaps not exist");
			if (dao.getGrade() == "3") {
				logger.info("No dummy problem set for 3rd grade ㅠㅠ : Random problem set returned");
				List<String> imsi = new ArrayList<String>();
				for (String c : chapterIdList.get(part)) {
					if (c.compareToIgnoreCase("중등-중2-2학") < 0) {
						imsi.add(c);
						System.out.println("imsi : " + imsi);
					}
				}
				Collections.shuffle(imsi);
				selected_chapter = imsi.get(0);
			} else {
				logger.info("No available chapter for the part : first chapter of the part is given");
				selected_chapter = chapterIdList.get(part).get(0);				
			}
		}
		System.out.println("selected_chapter : " + selected_chapter);
		
		// 해당하는 단원에 대한 문제 set 가져오기
		logger.info("Getting problem set...");
		ProblemSetDemo result;
		List<ProblemSetDemo> queryResult = problemSetRepository.findAllByChapter(selected_chapter);
		if (queryResult.size() != 0 && queryResult != null) {
			System.out.println("Available problem sets for the selected chapter : " + queryResult.toString());
			Collections.shuffle(queryResult);
			result = queryResult.get(0);
		} else {
			// No problem set for the selected_chapter
			logger.info("No problem set found for the selected_chapter : " + selected_chapter);
			Map<String, String> err = new HashMap<String, String>();
			err.put("error", "problem set : " + queryResult);
			list.add(err);
			return list;
		}
		
		// 문제 set의 각 문제에 대한 정보 불러오기
		List<String> prob_list = new ArrayList<String>();
		prob_list.add(result.getProb1Uuid());
		prob_list.add(result.getProb2Uuid());
		prob_list.add(result.getProb3Uuid());
//		System.out.println("selected problem set : " + resultDAO.toString() + "\n3 problem infos for the problem set : " + problemRepository.findAllById(prob_list));

		// 문제 set에 해당하는 문제 3개 불러오기 
		logger.info("Getting each problem info...");
		List<ProblemDemo> pdDaoList = (List<ProblemDemo>) problemRepository.findAllById(prob_list);
		if (pdDaoList == null) {
			logger.info("pdDaoList is null");
			Map<String, String> err = new HashMap<String, String>();
			err.put("error", "problemRepository.findAllById returned null");
			list.add(err);
			return list;
		} else if (pdDaoList.size() == 0) {
			logger.info("pdDaoList has no data returned");
			Map<String, String> err = new HashMap<String, String>();
			err.put("error", "no data for the problem IDs " + prob_list);
			list.add(err);
			return list;
		}
		
		// 정렬
		Collections.sort(pdDaoList, new Comparator<ProblemDemo>() {
			@Override
			public int compare(ProblemDemo pddao1, ProblemDemo pddao2) {
				return pddao2.getDifficulty().compareTo(pddao1.getDifficulty());
			}
		});
		
		// 각 문제 정보를 output format에 맞게 넣어줌
		for (ProblemDemo pddao : pdDaoList) {
			System.out.println("============= problem query result =============");
			System.out.println(pddao.toString());
			System.out.println("==================================================");
			
			// output 생성
			Map<String, String> map = new HashMap<String, String>();
			map.put("problemId", pddao.getProbUuid());
			map.put("chapterId", pddao.getChapter());
			map.put("chapter", pddao.getCurriculumDao().getChapter());
			map.put("uk", pddao.getUkDao().getUkName());
			map.put("ukId", pddao.getUkUuid());
			
//			// 아직 2학기 내용에 대한 문제유형이 등록되어 있지 않음.
//			if (pddao.getProbTypeUuid() != null) {
//				map.put("problemType", pddao.getTypeUkDao().getTypeUkName());				
//			} else {
//				map.put("problemType", "No problem type for 2nd semesters, yet.");
//			}
			
			map.put("difficulty", pddao.getDifficulty());
			list.add(map);
		}
		
		// 문제 정보 dummy data로 보여주기 - DB에 문제 정보가 없어서 힘듬.
		
//		if (chapterIdList.containsKey(part)) {
//			if (part == "0") {
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("problemId", "1987");
//				map.put("chapterId", "중등-중2-1학-01-01-03");
//				map.put("chapter", "유리수와 소수");
//				map.put("uk", "순환소수");
//				map.put("problemType", "순환소수를 포함한 식의 계산");
//				map.put("difficulty", "중");
//				list.add(map);
//				
//				Map<String, String> map1 = new HashMap<String, String>();
//				map1.put("problemId", "1939");
//				map1.put("chapterId", "중등-중2-1학-01-01-03");
//				map1.put("chapter", "유리수와 소수");
//				map1.put("uk", "순환소수를 분수로 나타내는 방법");
//				map1.put("problemType", "순환소수를 분수로 나타내기 공식 이용");
//				map1.put("difficulty", "상");
//				list.add(map1);
//				
//				Map<String, String> map2 = new HashMap<String, String>();
//				map2.put("problemId", "2088");
//				map2.put("chapterId", "중등-중1-1학-01-02-05");
//				map2.put("chapter", "수와 연산");
//				map2.put("uk", "유리수의 곱셈과 나눗셈의 혼합 계산");
//				map2.put("problemType", "어떤 수 구하기 곱셈, 나눗셈");
//				map2.put("difficulty", "하");
//				list.add(map2);		
//				
//			} else if (part == "1") {
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("problemId", "123");
//				map.put("chapterId", chapterIdList.get(part).get(1));
//				map.put("chapter", "일차방정식");
//				map.put("uk", "일차방정식");
//				map.put("problemType", "일차방정식과 관련된 문제유형");
//				map.put("difficulty", "중");
//				list.add(map);
//				
//				Map<String, String> map1 = new HashMap<String, String>();
//				map1.put("problemId", "456");
//				map1.put("chapterId", chapterIdList.get(part).get(2));
//				map1.put("chapter", "연립일차방정식");
//				map1.put("uk", "연립일차방정식");
//				map1.put("problemType", "연립일차방정식과 관련된 문제유형");
//				map1.put("difficulty", "상");
//				list.add(map1);
//				
//				Map<String, String> map2 = new HashMap<String, String>();
//				map2.put("problemId", "789");
//				map2.put("chapterId", chapterIdList.get(part).get(0));
//				map2.put("chapter", "문자와 식");
//				map2.put("uk", "문자");
//				map2.put("problemType", "문자와 관련된 문제유형");
//				map2.put("difficulty", "하");
//				list.add(map2);
//				
//			} else {
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("problemId", "2345");
//				map.put("chapterId", chapterIdList.get(part).get(1));
//				map.put("chapter", "aaaa");
//				map.put("uk", "bbbb");
//				map.put("problemType", "cccc");
//				map.put("difficulty", "중");
//				list.add(map);
//				
//				Map<String, String> map1 = new HashMap<String, String>();
//				map1.put("problemId", "235");
//				map1.put("chapterId", chapterIdList.get(part).get(2));
//				map1.put("chapter", "aaaaa");
//				map1.put("uk", "bbbbb");
//				map1.put("problemType", "ccccc");
//				map1.put("difficulty", "상");
//				list.add(map1);
//				
//				Map<String, String> map2 = new HashMap<String, String>();
//				map2.put("problemId", "34578");
//				map2.put("chapterId", chapterIdList.get(part).get(0));
//				map2.put("chapter", "aaaaaa");
//				map2.put("uk", "bbbbbb");
//				map2.put("problemType", "ccccccc");
//				map2.put("difficulty", "하");
//				list.add(map2);
//			}
//			
//		}		
		
		return list;
	}
	
	
	
}
