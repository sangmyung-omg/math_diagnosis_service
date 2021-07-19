package com.tmax.WaplMath.Recommend.util.schedule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.model.problem.ProblemType;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserExamScopeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;
import com.tmax.WaplMath.Recommend.repository.UserRepository;
import com.tmax.WaplMath.Recommend.util.ExamScope;
import com.tmax.WaplMath.Recommend.util.history.ScheduleHistoryManagerV1;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedule card configuration logic v1
 * @author Sangheon_lee
 */
@Slf4j
@Component
public class ScheduleConfiguratorV1 {

	// Hyperparameter
	private static final Integer MAX_CARD_NUM = 5;
	private static final Integer SUPPLE_CARD_TYPE_NUM = 3;
	private static final Float LOW_MASTERY_THRESHOLD = 1.0f;

	// Repository
	@Autowired
	private ProblemRepo problemRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ProblemTypeRepo problemTypeRepo;
	@Autowired
	private CurriculumRepository curriculumRepo;
	@Autowired
	private UserKnowledgeRepository userKnowledgeRepo;

	@Autowired
	ScheduleHistoryManagerV1 historyManager;

	public String userId;
	public String today;
	public @Getter Set<Integer> solvedProbIdSet;
	public @Getter Set<String> examSubSectionIdSet;

	public List<String> getSubSectionScope(String userId) throws Exception {
		List<String> subSectionIdList;
		// today
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String today = LocalDate.now().format(formatter);

		// Get solved problem set
		List<String> sourceTypeList = new ArrayList<String>(
			Arrays.asList("type_question", "supple_question", "mid_exam_question", "trial_exam_question"));
		try {
			this.solvedProbIdSet = historyManager.getSolvedProbIdSet(userId, today, "", sourceTypeList);
		} catch (Exception e) {
			throw e;
		}

		// Load user information from USER_MASTER TB
		User userInfo;
		try {
			userInfo = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			throw new Exception(String.format("userId = %s is not in USER_MASTER TB.", e.getMessage()));
		}

		// Check whether user exam information is null
		String grade = userInfo.getGrade();
		String semester = userInfo.getSemester();
		String currentCurriculumId = userInfo.getCurrentCurriculumId();
		if (currentCurriculumId == null || grade == null || semester == null) {
			throw new Exception("One of user's info is null. Call UserInfo PUT service first.");
		}

		// Get total normal subsectionList
		String endCurriculumId = ExamScope.examScope.get("3-2-final").get(1);
		subSectionIdList = curriculumRepo.findSubSectionListBetween(currentCurriculumId, endCurriculumId); // 그냥 학년 마지막까지
		log.info("전체 소단원 범위 = {}", subSectionIdList);
		return subSectionIdList;
	}

	public ScheduleConfigDTO getNormalScheduleConfig(String userId) throws Exception {
		ScheduleConfigDTO output = new ScheduleConfigDTO();
		List<CardConfigDTO> cardConfigList = new ArrayList<CardConfigDTO>();
		Set<String> addtlSubSectionIdSet = new HashSet<String>();

		List<String> subSectionIdList = getSubSectionScope(userId);
		log.info("전체 소단원 범위 = {}", subSectionIdList);

		// 중간평가 판단 - 중단원만 일단
		Set<String> sectionIdSet = new HashSet<String>(); // 토탈 범위 중 단원들
		subSectionIdList.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));
		log.info("1.지금부터 중 단원들 : " + sectionIdSet.toString());
		//		Set<String> sectionSet = new HashSet<String>(Arrays.asList("중등-중2-1학-01-02"));

		List<Integer> completedTypeIdList;
		try {
			completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", "type_question");
		} catch (Exception e) {
			throw e;
		}
		log.info("2. 유저가 푼 유형 UK들 : " + completedTypeIdList.toString());

		List<ProblemType> remainTypeList = problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, completedTypeIdList);
		Set<String> notDoneSectionIdSet = new HashSet<String>(); // 안푼 중단원들
		if (completedTypeIdList.size() == 0) {
			log.info("- 푼게 하나도 없음.");
			notDoneSectionIdSet.addAll(sectionIdSet);
		} else {
			remainTypeList.forEach(type -> notDoneSectionIdSet.add(type.getCurriculumId().substring(0, 14)));
		}
		log.info("3. 안푼 중 단원들 : " + notDoneSectionIdSet.toString());

		sectionIdSet.removeAll(notDoneSectionIdSet); // sectionSet에 완벽히 푼 단원들 저장됨
		log.info("4. 완벽히 유형카드를 푼 중 단원들 : " + sectionIdSet.toString());

		Set<String> completedSectionIdSet;
		try {
			completedSectionIdSet = historyManager.getCompletedSectionIdList(userId, today, "mid_exam_question");
		} catch (Exception e) {
			throw e;
		}
		log.info("5. 이미 중간평가 카드 푼 중 단원들 : " + completedSectionIdSet.toString());

		sectionIdSet.removeAll(completedSectionIdSet); // sectionSet에 완벽히 푼 단원들 - 이미 푼 중간평가 저장됨
		log.info("6. 중간평가 대상인 최종 중 단원 : " + sectionIdSet.toString());

		// 완벽히 푼 단원이 있으면 중간 평가
		if (sectionIdSet.size() != 0) {
			String sectionId = sectionIdSet.iterator().next();
			log.info("중간에 다 풀었으니까 중간평가 진행: " + sectionId);
			cardConfigList.add(CardConfigDTO.builder().cardType("midExam").curriculumId(sectionId).testType("section").build());
			addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}

		// 보충 필요한지 판단
		//		List<Integer> suppleTypeIdList = new ArrayList<Integer>(Arrays.asList());
		//		List<Integer> solvedTypeIdList = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 15, 17, 19));
		List<Integer> suppleTypeIdList;
		try {
			suppleTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", "supple_question");
		} catch (Exception e) {
			throw e;
		}
		log.info("7. 지금까지 보충 카드로 풀어본 유형ID 리스트 = ");
		log.info(suppleTypeIdList.toString());

		List<Integer> solvedTypeIdList;
		try {
			solvedTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", "type_question");
		} catch (Exception e) {
			throw e;
		}
		if (solvedTypeIdList.size() != 0) {
			List<TypeMasteryDTO> lowMasteryTypeList = userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, suppleTypeIdList,
				LOW_MASTERY_THRESHOLD);
			log.info("8. 보충 카드로 풀어본 유형 제외, 유형 카드들의 이해도 리스트 = ");
			for (TypeMasteryDTO typeMastery : lowMasteryTypeList)
				log.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));

			// 보충 채울만큼 넉넉하면 보충 카드
			if (lowMasteryTypeList.size() >= SUPPLE_CARD_TYPE_NUM) {
				log.info("	이해도 낮은게 많아서 보충 카드 진행");
				List<Integer> suppleCardTypeIdList = new ArrayList<Integer>();
				for (TypeMasteryDTO e : lowMasteryTypeList.subList(0, SUPPLE_CARD_TYPE_NUM))
					suppleCardTypeIdList.add(e.getTypeId());
				cardConfigList
					.add(CardConfigDTO.builder().cardType("supple").typeMasteryList(lowMasteryTypeList.subList(0, SUPPLE_CARD_TYPE_NUM)).build());
				addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));

				if (lowMasteryTypeList.size() >= SUPPLE_CARD_TYPE_NUM * 2) {
					log.info("	보충카드 하나 더 추가");
					suppleCardTypeIdList = new ArrayList<Integer>();
					for (TypeMasteryDTO e : lowMasteryTypeList.subList(SUPPLE_CARD_TYPE_NUM, SUPPLE_CARD_TYPE_NUM * 2))
						suppleCardTypeIdList.add(e.getTypeId());
					cardConfigList.add(CardConfigDTO.builder().cardType("supple")
						.typeMasteryList(lowMasteryTypeList.subList(SUPPLE_CARD_TYPE_NUM, SUPPLE_CARD_TYPE_NUM * 2)).build());
					addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
				}
			}
		}
		// 나머지 카드들 유형카드로 채우기
		List<Integer> noProbTypeIdList = new ArrayList<Integer>();
		// 공부 안한 유형uk가 있으면 유형카드
		if (remainTypeList.size() != 0) {
			for (ProblemType type : remainTypeList) {
				Integer typeId = type.getTypeId();
				log.info("중간평가 아니니까 유형 UK 카드 진행: " + typeId);
				List<Problem> typeProbList = problemRepo.NfindProbListByType(typeId, solvedProbIdSet);
				if (typeProbList.size() == 0) {
					noProbTypeIdList.add(typeId);
					continue;
				}
				cardConfigList.add(CardConfigDTO.builder().cardType("type").typeId(typeId).build());
				addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId).orElse(new ProblemType()).getCurriculumId());
				if (cardConfigList.size() == MAX_CARD_NUM)
					break;
			}
			log.info("문제가 없어서 못만든 유형UK: " + noProbTypeIdList);
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}
		output.setCardConfigList(cardConfigList);
		output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
		return output;
	}


	// set to dummy --> 4개 카드 종류별로 return
	public ScheduleConfigDTO getDummyScheduleConfig(String userId) throws Exception {
		ScheduleConfigDTO output = new ScheduleConfigDTO();
		List<CardConfigDTO> cardConfigList = new ArrayList<CardConfigDTO>();
		Set<String> addtlSubSectionIdSet = new HashSet<String>();

		List<String> subSectionIdList = getSubSectionScope(userId);

		User userInfo;
		try {
			userInfo = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			throw new Exception(String.format("userId = %s is not in USER_MASTER TB.", e.getMessage()));
		}

		// Check whether user exam information is null
		String grade = userInfo.getGrade();
		String semester = userInfo.getSemester();
		String currentCurriculumId = userInfo.getCurrentCurriculumId();
		if (currentCurriculumId == null || grade == null || semester == null) {
			throw new Exception("One of user's info is null. Call UserInfo PUT service first.");
		}

		// for trialExam card
		String examStartCurriculumId = ExamScope.examScope.get(String.format("%s-%s-%s", grade, semester, "final")).get(0);
		String examEndCurriculumId = ExamScope.examScope.get(String.format("%s-%s-%s", grade, semester, "final")).get(1);
		List<String> examSubSectionList = curriculumRepo.findSubSectionListBetween(examStartCurriculumId, examEndCurriculumId); // 그냥 학년 마지막까지
		this.examSubSectionIdSet = new HashSet<String>();
		this.examSubSectionIdSet.addAll(examSubSectionList);

		// 유형카드 : 첫 번째 유형
		List<ProblemType> remainTypeList = problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, null);
		Integer typeId = remainTypeList.get(0).getTypeId();
		log.info("중간평가 아니니까 유형 UK 카드 진행: " + typeId);
		cardConfigList.add(CardConfigDTO.builder().cardType("type").typeId(typeId).build());
		addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId).orElse(new ProblemType()).getCurriculumId());

		// 보충 카드 : 둘, 셋, 네번째 유형에 대해
		List<Integer> suppleTypeIdList = new ArrayList<Integer>();
		List<Integer> solvedTypeIdList = new ArrayList<Integer>(
			Arrays.asList(remainTypeList.get(1).getTypeId(), remainTypeList.get(2).getTypeId(), remainTypeList.get(3).getTypeId()));
		List<TypeMasteryDTO> lowMasteryTypeList = userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, suppleTypeIdList,
			LOW_MASTERY_THRESHOLD);
		log.info("8. 보충 카드로 풀어본 유형 제외, 유형 카드들의 이해도 리스트 = ");
		for (TypeMasteryDTO typeMastery : lowMasteryTypeList)
			log.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));
		log.info("	이해도 낮은게 많아서 보충 카드 진행");
		List<Integer> suppleCardTypeIdList = new ArrayList<Integer>();
		for (TypeMasteryDTO e : lowMasteryTypeList.subList(0, SUPPLE_CARD_TYPE_NUM))
			suppleCardTypeIdList.add(e.getTypeId());
		cardConfigList
			.add(CardConfigDTO.builder().cardType("supple").typeMasteryList(lowMasteryTypeList.subList(0, SUPPLE_CARD_TYPE_NUM)).build());
		addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));

		// 중간 평가 카드 (중단원)
		String sectionId = currentCurriculumId.substring(0, 14);
		log.info("중간평가 진행(중단원): " + sectionId);
		cardConfigList.add(CardConfigDTO.builder().cardType("midExam").curriculumId(sectionId).testType("section").build());
		addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));

		//		String chapterId = currentCurriculumId.substring(0, 11);
		//		log.info("\n중간평가 진행(대단원): " + chapterId);
		//		CardDTO midExamCard2 = cardManager.generateMidExamCard(chapterId, "chapter");
		//		cardList.add(midExamCard2);

		// 모의고사 카드
		String trialExamType = String.format("%s-%s-%s", grade, semester, "final");
		log.info("모의고사 진행: " + String.format("%s-%s-%s", grade, semester, "final"));
		cardConfigList.add(CardConfigDTO.builder().cardType("trialExam").examKeyword(trialExamType).build());
		addtlSubSectionIdSet.addAll(examSubSectionList);

		output.setCardConfigList(cardConfigList);
		output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
		return output;
	}

	public void getExamScheduleConfig() {

	}
}
