package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbDTO;
import com.tmax.WaplMath.Recommend.dto.waplscore.WaplScoreProbListDTO;
import com.tmax.WaplMath.Recommend.model.problem.ProblemType;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.TypeUkRelRepo;
import com.tmax.WaplMath.Recommend.util.ExamScope;

/**
 * Generate expected schedule uk list for calculate wapl score
 * @author Sangheon_lee
 */
@Slf4j
@Component
public class WaplScoreManager {
	//Hyperparameter
	private final Integer TYPE_CARD_NUM = 4;
	private final Integer TYPE_CARD_PROB_NUM = 5;
	private final Integer EXAM_CARD_PROB_NUM = 10;
	private final Integer MID_EXAM_CARD_PROB_NUM = 15;

	//Logging option
	private final Boolean printCardInfo = false; //level 1
	private final Boolean printUkInfo = false; //level 2
	private final Boolean printProbInfo = false; //level 3

	@Autowired
	TypeUkRelRepo typeUkRelRepo;
	@Autowired
	ProblemTypeRepo problemTypeRepo;
	@Autowired
	CurriculumRepository curriculumRepo;

	public Integer totalUkLength = 0;
	public Integer totalProbCnt = 0;

	public List<WaplScoreProbDTO> generateSubSectionCardProbList(String subSection, Integer MAX_PROB_NUM) {
		List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();
		int probCnt = 0;
		List<Integer> typeList = problemTypeRepo.findTypeIdListInSubSection(subSection);
		while (probCnt != EXAM_CARD_PROB_NUM) {
			Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
			List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
			if(ukList.size() != 0) {
				if (printProbInfo)
					log.info("	소단원 카드 문제 {}, 유형={}, uk추가: {}", probCnt + 1, typeId, ukList);
				cardProbDTOList.add(new WaplScoreProbDTO(ukList, "시험대비"));
			}
			probCnt += 1;
		}
		totalProbCnt += probCnt;
		if (printUkInfo)
			log.info("	probDTO 추가: {}", cardProbDTOList);
		return cardProbDTOList;
	}

	public List<WaplScoreProbDTO> generateSectionCardProbList(String section, String type) {
		List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();
		List<String> midSubSectionList = curriculumRepo.findSubSectionListInSection(section);
		Map<String, List<Integer>> subSectionTypeMap = new HashMap<String, List<Integer>>();
		int probCnt = 0;

		for (String subSection : midSubSectionList)
			subSectionTypeMap.put(subSection, problemTypeRepo.findTypeIdListInSubSection(subSection));

		while (probCnt != MID_EXAM_CARD_PROB_NUM) {
			String subSection = midSubSectionList.get(probCnt % midSubSectionList.size());
			List<Integer> typeList = subSectionTypeMap.get(subSection);

			Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
			List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
			if(ukList.size() != 0) {
				if (printProbInfo)
					log.info("	중간 평가 카드 문제 {}, 유형={}, uk추가: {}", probCnt + 1, typeId, ukList);
				cardProbDTOList.add(new WaplScoreProbDTO(ukList, type));
			}
			probCnt += 1;
		}
		totalProbCnt += probCnt;
		if (printUkInfo)
			log.info("	probDTO 추가: {}", cardProbDTOList);

		return cardProbDTOList;
	}

	public List<WaplScoreProbDTO> generateChapterCardProbList(String chapter, String type) {
		List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();
		List<String> midSectionList = curriculumRepo.findSectionListInChapter(chapter);
		Map<String, List<Integer>> sectionTypeMap = new HashMap<String, List<Integer>>();
		int probCnt = 0;

		for (String section : midSectionList)
			sectionTypeMap.put(section, problemTypeRepo.findTypeIdListInSection(section));

		while (probCnt != MID_EXAM_CARD_PROB_NUM) {
			String section = midSectionList.get(probCnt % midSectionList.size());
			List<Integer> typeList = sectionTypeMap.get(section);

			Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
			List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
			if(ukList.size() != 0) {
				if (printProbInfo)
					log.info("	중간 평가 카드 문제 {}, 유형={}, uk추가: {}", probCnt + 1, typeId, ukList);
				cardProbDTOList.add(new WaplScoreProbDTO(ukList, type));
			}
			probCnt += 1;
		}
		totalProbCnt += probCnt;
		if (printUkInfo)
			log.info("	probDTO 추가: {}", cardProbDTOList);

		return cardProbDTOList;
	}

	public List<WaplScoreProbDTO> generateTrialExamCardProbList(List<String> examSubSectionList) {
		List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();
		int probCnt = 0;
		List<Integer> typeList = problemTypeRepo.findTypeIdListInSubSectionList(examSubSectionList);

		while (probCnt != EXAM_CARD_PROB_NUM * 2) {
			Integer typeId = typeList.get(new Random().nextInt(typeList.size()));
			List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
			if(ukList.size() != 0) {
				if (printProbInfo)
					log.info("	모의고사 카드 문제 {}, 유형={}, uk추가: {}", probCnt + 1, typeId, ukList);
				cardProbDTOList.add(new WaplScoreProbDTO(ukList, "시험대비"));
			}
			probCnt += 1;
		}
		totalProbCnt += probCnt;
		if (printUkInfo)
			log.info("	probDTO 추가: {}", cardProbDTOList);

		return cardProbDTOList;
	}

	public WaplScoreProbListDTO getWaplScoreProbList(String targetExam, String currentCurriculumId, Integer remainDay) {
		WaplScoreProbListDTO output = new WaplScoreProbListDTO();
		List<WaplScoreProbDTO> probList = new ArrayList<WaplScoreProbDTO>();

		//실력 향상
		if (remainDay > 14) {
			String EndCurrId = ExamScope.examScope.get("3-2-final").get(1);
			List<String> subSectionList = curriculumRepo.findSubSectionListBetween(currentCurriculumId, EndCurrId); // 이번학기마지막까지
			Integer normalScheduleDay = remainDay - 14;
			List<ProblemType> typeIdList = problemTypeRepo.findTypeListInSubSectionList(subSectionList);
			if (printCardInfo)
				log.info("typeId 개수 = {}", typeIdList.size());

			String prevChapter = typeIdList.get(0).getCurriculumId().substring(0, 11);
			String prevSection = typeIdList.get(0).getCurriculumId().substring(0, 14);
			Integer nextTypeIdx = 0;

			for (int cnt = 0; cnt < normalScheduleDay; cnt++) {
				int cardCnt = 1;
				if (nextTypeIdx > typeIdList.size() - 1) {
					log.info("실력향상 할 유형이 없어서 시험대비로 넘어감");
					break;
				}
				ProblemType firstType = typeIdList.get(nextTypeIdx);
				String currentChapter = firstType.getCurriculumId().substring(0, 11);
				String currentSection = firstType.getCurriculumId().substring(0, 14);
				List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();

				if (!currentChapter.equals(prevChapter)) {
					//대단원 평가
					if (printCardInfo)
						log.info("실력향상 {}번째 날 중간 평가 카드 (대단원={})", cnt + 1, prevChapter);
					cardProbDTOList = generateChapterCardProbList(prevChapter, "실력향상");
				} else if (!currentSection.equals(prevSection)) {
					//중단원 평가
					if (printCardInfo)
						log.info("실력향상 {}번째 날 중간 평가 카드 (중단원={})", cnt + 1, prevSection);
					cardProbDTOList = generateSectionCardProbList(prevSection, "실력향상");
				} else {
					for (ProblemType type : typeIdList.subList(nextTypeIdx, Math.min(nextTypeIdx + TYPE_CARD_NUM, typeIdList.size()))) {
						Integer typeId = type.getTypeId();
						List<Integer> ukList = typeUkRelRepo.findAllUkByTypeId(typeId);
						if(ukList.size() != 0) {
							if (printCardInfo)
								log.info("실력향상 {}번째 날 {}번째 유형 카드 (유형={}, 소단원={}) uk 추가: {}", cnt + 1, cardCnt++, typeId, type.getCurriculumId(),
									ukList);
							for (int i = 0; i < TYPE_CARD_PROB_NUM; i++)
								cardProbDTOList.add(new WaplScoreProbDTO(ukList, "실력향상"));
						}
						totalProbCnt += TYPE_CARD_PROB_NUM;
					}
					nextTypeIdx += TYPE_CARD_NUM;
				}
				probList.addAll(cardProbDTOList);
				prevChapter = currentChapter;
				prevSection = currentSection;
				if (printCardInfo)
					log.info("");
			}
		}
		log.info("실력향상 문제 수 = {}", totalProbCnt);
		log.info("=========================");

		//시험 대비
		String examStartCurriculumId = ExamScope.examScope.get(targetExam).get(0);
		String examEndCurriculumId = ExamScope.examScope.get(targetExam).get(1);
		List<String> examSubSectionList = curriculumRepo.findSubSectionListBetween(examStartCurriculumId, examEndCurriculumId); // 시험 범위

		if (printCardInfo)
			log.info("시험대비 소단원: {}", examSubSectionList);
		int numSubSection = examSubSectionList.size();
		String prevSection = examSubSectionList.get(0).substring(0, 14);
		int idx = 0;
		int examDay = 0;

		while (examDay != 14) {
			List<WaplScoreProbDTO> cardProbDTOList = new ArrayList<WaplScoreProbDTO>();

			if (idx <= numSubSection) {
				String currentSubSection = idx == numSubSection ? "" : examSubSectionList.get(idx);
				String currentSection = idx == numSubSection ? "" : currentSubSection.substring(0, 14);

				//중간평가
				if (!prevSection.equals(currentSection)) {
					if (printCardInfo)
						log.info("시험대비 {}번째 날 중간 평가 카드 (중단원={})", examDay + 1, prevSection);
					cardProbDTOList = generateSectionCardProbList(prevSection, "시험대비");
					probList.addAll(cardProbDTOList);
					if (idx == numSubSection)
						idx += 1;
				} else {
					String nextSubSection = idx + 1 > numSubSection - 1 ? "" : examSubSectionList.get((idx + 1) % numSubSection);
					String nextSection = idx + 1 > numSubSection - 1 ? "" : nextSubSection.substring(0, 14);

					if (currentSection.equals(nextSection)) {
						// 두 개의 소단원 카드 제공
						int cardCnt = 1;
						for (String subSection : Arrays.asList(currentSubSection, nextSubSection)) {
							if (printCardInfo)
								log.info("시험대비 {}번째 날 {}번째 소단원 카드 (소단원={})", examDay + 1, cardCnt, subSection);
							cardProbDTOList = generateSubSectionCardProbList(subSection, EXAM_CARD_PROB_NUM);
							probList.addAll(cardProbDTOList);
							cardCnt += 1;
						}
						idx += 1;
					} else {
						// 한 개의 소단원 카드 제공
						if (printCardInfo)
							log.info("시험대비 {}번째 날 {}번째 소단원 카드 (소단원={})", examDay + 1, 1, currentSubSection);
						cardProbDTOList = generateSubSectionCardProbList(currentSubSection, EXAM_CARD_PROB_NUM * 2);
						probList.addAll(cardProbDTOList);
					}
					idx += 1;
				}
				prevSection = currentSection;
			} else {
				if (printCardInfo)
					log.info("시험대비 {}번째 날 모의고사 카드", examDay + 1);
				cardProbDTOList = generateTrialExamCardProbList(examSubSectionList);
				probList.addAll(cardProbDTOList);
			}
			examDay += 1;
		}

		log.info("최종 문제 수 = {}", totalProbCnt);
		assert (totalProbCnt == probList.size());
		output.setProbList(probList);
		probList.forEach(e -> totalUkLength += e.getUkList().size());
		log.info("최종 UK sequence 길이 = {}", totalUkLength);

		return output;
	}
}
