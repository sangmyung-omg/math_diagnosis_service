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
import com.tmax.WaplMath.Recommend.model.ChapterMasteryDTO;
import com.tmax.WaplMath.Recommend.model.Uk;
import com.tmax.WaplMath.Recommend.model.UserKnowledge;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.UkRepository;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;


@Service
public class CurriculumService {
	
	/*
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	@Autowired
	CurriculumRepository curriculumRepository;
	
	@Autowired
	UkRepository ukRepository;
	
	@Autowired
	UserKnowledgeRepository userKnowledgeRepository;
	
	@Autowired
	MasteryService masteryService;
		
	public Map<String, Object> getChapterMastery(String userId, List<String> ukIdList){
		
		// chapter & part mapping 정보 불러오기 (DB에 있으면, 아래에서 단원, UK와 함께 불러올 수 있음)
		DemoChapterPartMapper chapterPartMapper = new DemoChapterPartMapper();
		Map<String, List<String>> chapterPartList = chapterPartMapper.getMappingInfo();
//		System.out.println(chapterIdList);
		
		
		// UK 리스트가 어떤 단원에 맵핑되어 있는지 불러오기
		logger.info("Getting uk info...");
		List<Uk> ukInfoList = (List<Uk>) ukRepository.findAllById(ukIdList);
//		System.out.println("ukInfoList : " + ukInfoList.size());
		
		// 각 UK들이 어떤 대단원에 속해 있는지 대단원 ID 별로 맵핑 정보 생성
		Map<String, List<String>> ukChapterMap = new HashMap<String, List<String>>();
		for (Uk dao : ukInfoList) {
			if (!ukChapterMap.containsKey(dao.getCurriculum().getChapterId())) {
				List<String> l = new ArrayList<String>();
				l.add(dao.getUkName());
				ukChapterMap.put(dao.getCurriculum().getChapterId(), l);
			} else {
				ukChapterMap.get(dao.getCurriculum().getChapterId()).add(dao.getUkName());
			}
		}
		
		// 각 대단원에 속하는 UK들 조회  (UK 정보 + 각 UK에 해당하는 mastery 정보)
//		logger.info("currIdList : " + currIdList);
		List<ChapterMasteryDTO> result = new ArrayList<ChapterMasteryDTO>();
		
		logger.info("Getting all UKs of chapters and their mastery levels...");
		for (String currId : ukChapterMap.keySet()) {
			// 해당 유저 ID와 대단원 ID에 해당하는 UK 정보 및 mastery 정보 조회
			List<UserKnowledge> ukMasteryForChapters = (List<UserKnowledge>) userKnowledgeRepository.findAllByUserAndChapter(userId, currId);
			
			// DTO template 생성
			ChapterMasteryDTO template = new ChapterMasteryDTO();
			List<String> template_uklist = new ArrayList<String>();
			List<Float> template_masterylist = new ArrayList<Float>();
			template.setUkList(template_uklist);
			template.setMasteryList(template_masterylist);
			
			// 반드시 표출해야 하는 진단고사에서 푼 UK들 자리 우선적 확보.
			List<String> solved_uk = ukChapterMap.get(currId);
			for (int i=0; i < solved_uk.size(); i++) {
				template.getUkList().add("a");
				template.getMasteryList().add(0f);
			}
			
			int flag = 0;
			for (UserKnowledge dao : ukMasteryForChapters) {
//				logger.info("UK Info & Mastery query result : \n" + dao.getUkUuid() + ", "
//						+ dao.getUkDao().getUkName() + ", " + Float.toString(dao.getUkMastery()) + ", "
//						+ dao.getUkDao().getCurriculumDao().getChapterId() + ", " + dao.getUkDao().getCurriculumDao().getChapter());
				
				// 단원 dependent 한 정보 (part & chapterName) 는 처음에 한 번만 set.
				if (flag == 0) {
					// part
					for (String key : chapterPartList.keySet()) {
						if (chapterPartList.get(key).contains(dao.getUk().getCurriculum().getChapterId())) {
							template.setPart(key);
						}
					}
					// chapterName
					template.setChapterName(dao.getUk().getCurriculum().getChapter());
					flag += 1;
				}
				
				// ukList
				template.getUkList().add(dao.getUk().getUkName());
				
				// masteryList
				template.getMasteryList().add(dao.getUkMastery());
				
				// 푼 문제의 UK들이 있으면 순서를 앞으로 가저오기.
				if (solved_uk.contains(dao.getUk().getUkName())) {
					int flag2 = 0;
					for (String ukName : solved_uk) {
						if (template.getUkList().contains(ukName)) {
							Collections.swap(template.getUkList(), template.getUkList().indexOf("a"), template.getUkList().indexOf(ukName));
							Collections.swap(template.getMasteryList(), template.getUkList().indexOf("a"), template.getUkList().indexOf(ukName));
							flag2 += 1;
						} else break;
					}
					// 표출할 UK 개수 10개 이상 되도록 (총 UK가 10개 안되면 그냥 모든 UK 표출) - 혹시 나중에 쓰일 수 있어서
					// if (flag2 == solved_uk.size() && template.getUkList().size() >= 10) {
					// 데모 : 푼 문제에 해당하는 UK 다 모이면 break;
					if (flag2 == solved_uk.size()) {
//						logger.info("^^^^^^^^^^^^^^^^ BREAK; ^^^^^^^^^^^^^^^^");
						break;
					}					
				}
			}
			
			// 후처리 (initial 자리 위한 "a" 제거)
			for (int i=0; i < solved_uk.size(); i++) {
				template.getUkList().remove("a");
				template.getMasteryList().remove(0f);
			}
			*/
			
			/*
			// UK와 Mastery 리스트의 총 길이 최대 10개로 균일하게 맞추기
			if (template.getUkList().size() > 10) {
				template.setUkList(template.getUkList().subList(0, 10));
				template.setMasteryList(template.getMasteryList().subList(0, 10));				
			}
			*/
			
	/*
			// 일단, 진단고사 데모에서는 푼 문제(에 태그된 UK)에 대해서만 반환하자.
			template.setUkList(template.getUkList().subList(0, solved_uk.size()));
			template.setMasteryList(template.getMasteryList().subList(0, solved_uk.size()));
			
			// chapterMastery (일단 그 단원의 모든 UK에 대해 구함)
			Float sum = 0f;
			for (Float m : template.getMasteryList()) {
				sum += m;
			}
			template.setChapterMastery(sum / template.getMasteryList().size());

			// 리스트에 dto 담기
			result.add(template);
		}
//		logger.info(result.toString());
		
		Map<String, Object> re = new HashMap<String, Object>();
		
		// 정렬
		Collections.sort(result, new Comparator<ChapterMasteryDTO>() {
			@Override
			public int compare(ChapterMasteryDTO cmdto1, ChapterMasteryDTO cmdto2) {
				return cmdto1.getPart().compareTo(cmdto2.getPart());
			}
		});
//		re.put("chapterMasteryList", dtoList);
		
		// 데모용 점수 수정 (보기좋게)
		for (ChapterMasteryDTO dto : result) {
			Float chapterMastery = dto.getChapterMastery();
			int i = 0;
			for (Float m : dto.getMasteryList()) {
//				logger.info(dto.getUkList().get(dto.getMasteryList().indexOf(m)) + ", " + Float.toString(m));
//				dto.getMasteryList().set(i, (-8/3)*m*m +(74/15)*m-1.3f);
//				logger.info(dto.getUkList().get(dto.getMasteryList().indexOf(dto.getMasteryList().get(i))) + ", " + Float.toString(m));
				dto.getMasteryList().set(i, 1.6f*m-0.3f);
				i += 1;
			}
//			logger.info(dto.getChapterName() + ", " + Float.toString(dto.getChapterMastery()));
//			dto.setChapterMastery((-8/3)*chapterMastery*chapterMastery +(74/15)*chapterMastery-1.3f);
//			logger.info(dto.getChapterName() + ", " + Float.toString(dto.getChapterMastery()));
			dto.setChapterMastery(1.6f*chapterMastery-0.3f);
		}
		
		
		re.put("chapterMasteryList", result);
		re.put("resultMessage", "successfully returned");
		return re;
	}
	
	public List<String> getChapterNameList(String grade, String semester) {
		List<String> list = new ArrayList<String>();
		String queryString = "중등-중" + grade + "-" + semester + "학-%";
		logger.info("Getting chapter name list...");
		List<String> queryList = curriculumRepository.findAllByCurriculumIdLike(queryString);
		for (String str : queryList) {
			list.add(str);
		}

		return list;
	}
			*/
}
