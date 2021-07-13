package com.tmax.WaplMath.Recommend.util.schedule;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tmax.WaplMath.Recommend.dto.mastery.TypeMasteryDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.model.problem.ProblemType;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.model.user.UserExamScope;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.UserExamScopeRepo;
import com.tmax.WaplMath.Recommend.repository.UserKnowledgeRepository;
import com.tmax.WaplMath.Recommend.repository.UserRepository;
import com.tmax.WaplMath.Recommend.util.ExamScope;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Schedule card configuration logic v2
 * @author Sangheon_lee
 * @since 2021-06-30
 */
@Slf4j
@Component
public class ScheduleConfiguratorV2 {

	// Repository
	@Autowired
	private ProblemRepo problemRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ProblemTypeRepo problemTypeRepo;
	@Autowired
	private UserExamScopeRepo userExamScopeRepo;
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

	public User getValidUserInfo(String userId) throws Exception {
		User userInfo;
		try {
			userInfo = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			throw new Exception(String.format("userId = %s is not in USER_MASTER TB.", e.getMessage()));
		}
		// Check whether user exam information is null
		String grade = userInfo.getGrade();
		String semester = userInfo.getSemester();
		String examType = userInfo.getExamType();
		String currentCurriculumId = userInfo.getCurrentCurriculumId();
		if (examType == null || currentCurriculumId == null || grade == null || semester == null) {
			log.info("{}, {}, {}, {}", grade, semester, currentCurriculumId, examType);
			throw new Exception("One of user's info is null. Call UserInfo PUT service first.");
		} else {
			return userInfo;
		}
	}

	public UserExamScope getValidUserExamInfo(String userId) throws Exception {
		UserExamScope userExamScopeInfo;
		try {
			userExamScopeInfo = userExamScopeRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			throw new Exception(String.format("userId = %s is not in USER_EXAM_SCOPE TB.", e.getMessage()));
		}
		// Check whether user exam information is null
		//		Timestamp examStartDate = userExamScopeInfo.getUser().getExamStartDate();
		//		Timestamp examDueDate = userExamScopeInfo.getUser().getExamDueDate();
		String startSubSectionId = userExamScopeInfo.getStartSubSectionId();
		String endSubSectionId = userExamScopeInfo.getEndSubSectionId();
		//		if (startSubSectionId == null || endSubSectionId == null || examStartDate == null || examDueDate == null) {
		if (startSubSectionId == null || endSubSectionId == null) {
			log.info("{}, {}", startSubSectionId, endSubSectionId);
			throw new Exception("One of user's exam info is null. Call UserInfo PUT service first.");
		} else {
			return userExamScopeInfo;
		}
	}

	public void setUserSolvedProbIdSet(String userId) throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String today = LocalDate.now().format(formatter);
		this.today = today;
		// Get solved problem set
		List<String> sourceTypeList =
				new ArrayList<String>(Arrays.asList(CardConstants.TYPE_CARD_TYPESTR + "_question",
						CardConstants.SUPPLE_CARD_TYPESTR + "_question",
						CardConstants.SECTION_TEST_CARD_TYPESTR + "_question",
						CardConstants.CHAPTER_TEST_CARD_TYPESTR + "_question",
						CardConstants.TRIAL_EXAM_CARD_TYPESTR + "_question"));
		try {
			this.solvedProbIdSet = historyManager.getSolvedProbIdSet(userId, today, "", sourceTypeList);
		} catch (Exception e) {
			throw e;
		}
	}

	public void setExamSubSectionIdSet(UserExamScope userExamScopeInfo) {
		List<String> examSubSectionIdList = null;
		String startSubSectionId = userExamScopeInfo.getStartSubSectionId();
		String endSubSectionId = userExamScopeInfo.getEndSubSectionId();
		String exceptSubSectionIdStr = userExamScopeInfo.getExceptSubSectionIdList();
		List<String> exceptSubSectionIdList;
		if (exceptSubSectionIdStr != null) {
			exceptSubSectionIdList = Arrays.asList(exceptSubSectionIdStr.split(", "));
			log.info("제외되는 소단원 = {}", exceptSubSectionIdList);
			examSubSectionIdList = curriculumRepo.findSubSectionListBetweenExcept(startSubSectionId, endSubSectionId, exceptSubSectionIdList); // 이번 학기 마지막까지
		} else {
			examSubSectionIdList = curriculumRepo.findSubSectionListBetween(startSubSectionId, endSubSectionId); // 이번 학기 마지막까지
		}
		this.examSubSectionIdSet = new HashSet<String>();
		this.examSubSectionIdSet.addAll(examSubSectionIdList);
		log.info("시험 소단원 범위 = {}", examSubSectionIdSet);
	}

	public List<String> getNormalSubSectionList(String userId) throws Exception {
		List<String> subSectionIdList;
		User userInfo = getValidUserInfo(userId);
		String endCurriculumId = ExamScope.examScope.get(userInfo.getGrade() + "-" + userInfo.getSemester() + "-" + "final").get(1);
		subSectionIdList = curriculumRepo.findSubSectionListBetween(userInfo.getCurrentCurriculumId(), endCurriculumId); // 이번 학기 마지막까지
		log.info("전체 소단원 범위 = {}", subSectionIdList);
		return subSectionIdList;
	}

	public String getUserExamKeyword(String userId) throws Exception {
		User userInfo = getValidUserInfo(userId);
		return String.format("%s-%s-%s", userInfo.getGrade(), userInfo.getSemester(), userInfo.getExamType());
	}

	public ScheduleConfigDTO getNormalScheduleConfig(String userId) throws Exception {
		ScheduleConfigDTO output = new ScheduleConfigDTO();
		List<CardConfigDTO> cardConfigList = new ArrayList<CardConfigDTO>();
		Set<String> addtlSubSectionIdSet = new HashSet<String>();
		Integer totalCardProbNum = 0;
		setUserSolvedProbIdSet(userId);
		// 학년 학기 내 소단원 목록들
		List<String> subSectionIdList = getNormalSubSectionList(userId);
		log.info("전체 소단원 범위 = {}", subSectionIdList);
		// 중간평가 판단 - 중단원만 일단
		Set<String> sectionIdSet = subSectionIdList.stream().map(subSection -> subSection.substring(0, 14)).collect(Collectors.toSet());
		log.info("1.지금부터 중 단원들 : " + sectionIdSet.toString());
		//		Set<String> sectionSet = new HashSet<String>(Arrays.asList("중등-중2-1학-01-02"));

		List<Integer> completedTypeIdList;
		try {
			completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", CardConstants.TYPE_CARD_TYPESTR + "_question");
		} catch (Exception e) {
			throw e;
		}
		log.info("2. 유저가 푼 유형 UK들 : " + completedTypeIdList.toString());

		List<ProblemType> remainTypeList = problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, completedTypeIdList);
		Set<String> notDoneSectionIdSet = new HashSet<String>(); // 안푼 중단원들
		if (completedTypeIdList.size() == 0) {
			log.info("- 푼게 하나도 없음.");
			notDoneSectionIdSet.addAll(sectionIdSet);
		} else
			remainTypeList.forEach(type -> notDoneSectionIdSet.add(type.getCurriculumId().substring(0, 14)));
		log.info("3. 안푼 중 단원들 : " + notDoneSectionIdSet.toString());

		sectionIdSet.removeAll(notDoneSectionIdSet); // sectionSet에 완벽히 푼 단원들 저장됨
		log.info("4. 완벽히 유형카드를 푼 중 단원들 : " + sectionIdSet.toString());

		Set<String> completedSectionIdSet;
		try {
			completedSectionIdSet = historyManager.getCompletedSectionIdList(userId, today, CardConstants.SECTION_TEST_CARD_TYPESTR + "_question");
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
			cardConfigList.add(CardConfigDTO.builder()
											.cardType(CardConstants.SECTION_TEST_CARD_TYPESTR)
											.curriculumId(sectionId)
											.build());
			addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}

		// 보충 필요한지 판단
		List<Integer> suppleTypeIdList;
		try {
			suppleTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", CardConstants.SUPPLE_CARD_TYPESTR + "_question");
		} catch (Exception e) {
			throw e;
		}
		log.info("7. 지금까지 보충 카드로 풀어본 유형ID 리스트 = ");
		log.info(suppleTypeIdList.toString());

		List<Integer> solvedTypeIdList;
		try {
			solvedTypeIdList = historyManager.getCompletedTypeIdListAfterSuppleCard(userId, today);
		} catch (Exception e) {
			throw e;
		}
		solvedTypeIdList.removeAll(suppleTypeIdList);
		if (solvedTypeIdList.size() != 0) {
			List<TypeMasteryDTO> lowTypeMasteryList = userKnowledgeRepo.findNLowTypeMasteryList(userId, solvedTypeIdList,
				CardConstants.MASTERY_LOW_THRESHOLD);
			log.info("8. 보충 카드로 풀어본 유형 제외, 낮은 유형 카드들의 이해도 리스트 = ");
			for (TypeMasteryDTO typeMastery : lowTypeMasteryList)
				log.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));

			// 보충 채울만큼 넉넉하면 보충 카드
			if (lowTypeMasteryList.size() >= CardConstants.SUPPLE_CARD_TYPE_NUM) {
				log.info("	이해도 낮은게 많아서 보충 카드 진행");
				List<TypeMasteryDTO> typeMasteryList = lowTypeMasteryList.subList(0, CardConstants.SUPPLE_CARD_TYPE_NUM);
				List<Integer> suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
				cardConfigList.add(CardConfigDTO.builder()
												.cardType(CardConstants.SUPPLE_CARD_TYPESTR)
												.typeMasteryList(typeMasteryList)
												.build());
				addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
				totalCardProbNum += CardConstants.SUPPLE_CARD_TYPE_NUM * CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE;

				if (lowTypeMasteryList.size() >= CardConstants.SUPPLE_CARD_TYPE_NUM * 2) {
					log.info("	보충카드 하나 더 추가");
					typeMasteryList = lowTypeMasteryList.subList(CardConstants.SUPPLE_CARD_TYPE_NUM, CardConstants.SUPPLE_CARD_TYPE_NUM * 2);
					suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
					cardConfigList.add(CardConfigDTO.builder()
													.cardType(CardConstants.SUPPLE_CARD_TYPESTR)
													.typeMasteryList(typeMasteryList)
													.build());
					addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
					totalCardProbNum += CardConstants.SUPPLE_CARD_TYPE_NUM * CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE;
				}
			}

			// 많이 풀었는데 보충이 안나타나도 보충 카드
			else if (solvedTypeIdList.size() >= CardConstants.SUPPLE_CARD_TYPE_THRESHOLD) {
				log.info("	{}개 이상 잘하고 있으니 보충 카드 진행", CardConstants.SUPPLE_CARD_TYPE_THRESHOLD);
				List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findBottomTypeMasteryList(userId, solvedTypeIdList,
					CardConstants.SUPPLE_CARD_TYPE_NUM);
				log.info("	유형 카드들의 이해도 리스트 = ");
				for (TypeMasteryDTO typeMastery : typeMasteryList)
					log.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));
				List<Integer> suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
				cardConfigList.add(CardConfigDTO.builder()
												.cardType(CardConstants.SUPPLE_CARD_TYPESTR)
												.typeMasteryList(typeMasteryList)
												.build());
				addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
				totalCardProbNum += CardConstants.SUPPLE_CARD_TYPE_NUM * CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE;
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
				cardConfigList.add(CardConfigDTO.builder()
												.cardType(CardConstants.TYPE_CARD_TYPESTR)
												.typeId(typeId)
												.build());
				addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId).orElse(new ProblemType()).getCurriculumId());
				totalCardProbNum += CardConstants.MAX_TYPE_CARD_PROB_NUM;
				if (totalCardProbNum >= CardConstants.MAX_CARD_PROB_NUM)
					break;
			}
			log.info("문제가 없어서 못만든 유형UK: " + noProbTypeIdList);
		}
		// 유형도 다풀었는데 문제가 안채워지면 추가 보충카드
		if (totalCardProbNum < CardConstants.MAX_CARD_PROB_NUM) {
			String examKeyword = getUserExamKeyword(userId);
			String examStartSubSectionId = ExamScope.examScope.get(examKeyword).get(0);
			String examEndSubSectionId = ExamScope.examScope.get(examKeyword).get(1);
			Integer addtiTypeNum = (int) Math.ceil((CardConstants.MAX_CARD_PROB_NUM - totalCardProbNum) / 2.0);
			List<TypeMasteryDTO> addtiTypeMasteryList = userKnowledgeRepo
				.findTypeMasteryListBetween(userId, examStartSubSectionId, examEndSubSectionId).subList(0, addtiTypeNum);
			List<Integer> addtlSuppleCardTypeIdList = addtiTypeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
			cardConfigList.add(CardConfigDTO.builder()
										    .cardType(CardConstants.ADDTL_SUPPLE_CARD_TYPESTR)
										    .typeMasteryList(addtiTypeMasteryList)
										    .build());
			addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));
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
		UserExamScope userExamScopeInfo = getValidUserExamInfo(userId);
		setUserSolvedProbIdSet(userId);
		setExamSubSectionIdSet(userExamScopeInfo);
		// get SubSection List
		User userInfo = getValidUserInfo(userId);
		String currentCurriculumId = userInfo.getCurrentCurriculumId();
		List<String> subSectionIdList = getNormalSubSectionList(userId);

		// 유형카드 : 첫 번째 유형
		List<ProblemType> remainTypeList = problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, null);
		Integer typeId = remainTypeList.get(0).getTypeId();
		log.info("중간평가 아니니까 유형 UK 카드 진행: " + typeId);
		cardConfigList.add(CardConfigDTO.builder().cardType(CardConstants.TYPE_CARD_TYPESTR).typeId(typeId).build());
		addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId).orElse(new ProblemType()).getCurriculumId());

		// 보충 카드 : 둘, 셋, 네번째 유형에 대해
		List<Integer> suppleTypeIdList = new ArrayList<Integer>();
		List<Integer> solvedTypeIdList = new ArrayList<Integer>(
			Arrays.asList(remainTypeList.get(1).getTypeId(), remainTypeList.get(2).getTypeId(), remainTypeList.get(3).getTypeId()));
		List<TypeMasteryDTO> lowMasteryTypeList = userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, suppleTypeIdList,
			1.0f);
		log.info("8. 보충 카드로 풀어본 유형 제외, 유형 카드들의 이해도 리스트 = ");
		for (TypeMasteryDTO typeMastery : lowMasteryTypeList)
			log.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));
		log.info("	이해도 낮은게 많아서 보충 카드 진행");
		List<TypeMasteryDTO> typeMasteryList = lowMasteryTypeList.subList(0, CardConstants.SUPPLE_CARD_TYPE_NUM);
		List<Integer> suppleCardTypeIdList = typeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
		cardConfigList.add(CardConfigDTO.builder().cardType(CardConstants.SUPPLE_CARD_TYPESTR).typeMasteryList(typeMasteryList).build());
		addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));

		// 중간 평가 카드 (중단원)
		String sectionId = currentCurriculumId.substring(0, 14);
		log.info("중간평가 진행(중단원): " + sectionId);
		cardConfigList.add(CardConfigDTO.builder().cardType(CardConstants.SECTION_TEST_CARD_TYPESTR).curriculumId(sectionId).build());
		addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));

		// 추가 보충 카드
		String startSubSectionId = userExamScopeInfo.getStartSubSectionId();
		String endSubSectionId = userExamScopeInfo.getEndSubSectionId();
		Integer addtiTypeNum = (int) Math.ceil((CardConstants.MAX_CARD_PROB_NUM - 4) / 2.0);
		List<TypeMasteryDTO> addtiTypeMasteryList = userKnowledgeRepo.findTypeMasteryListBetween(userId, startSubSectionId, endSubSectionId)
			.subList(0, addtiTypeNum);
		List<Integer> addtlSuppleCardTypeIdList = addtiTypeMasteryList.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
		cardConfigList.add(CardConfigDTO.builder().cardType(CardConstants.ADDTL_SUPPLE_CARD_TYPESTR).typeMasteryList(addtiTypeMasteryList).build());
		addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));

		// 모의고사 카드
		String userExamKeyword = getUserExamKeyword(userId);
		log.info("모의고사 진행: " + userExamKeyword);
		cardConfigList.add(CardConfigDTO.builder().cardType(CardConstants.TRIAL_EXAM_CARD_TYPESTR).examKeyword(userExamKeyword).build());
		addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);

		output.setCardConfigList(cardConfigList);
		output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
		return output;
	}

	public ScheduleConfigDTO getExamScheduleConfig(String userId) throws Exception {
		ScheduleConfigDTO output = new ScheduleConfigDTO();
		List<CardConfigDTO> cardConfigList = new ArrayList<CardConfigDTO>();
		Set<String> addtlSubSectionIdSet = new HashSet<String>();
		// get user exam info
		UserExamScope userExamScopeInfo = getValidUserExamInfo(userId);
		setUserSolvedProbIdSet(userId);
		setExamSubSectionIdSet(userExamScopeInfo);
		// check days to prepare exam
		Timestamp examStartDate = userExamScopeInfo.getUser().getExamStartDate();
		Timestamp examDueDate = userExamScopeInfo.getUser().getExamDueDate();
		Period totalPeriod = Period.between(examStartDate.toLocalDateTime().toLocalDate(), examDueDate.toLocalDateTime().toLocalDate());
		Integer totalDays = totalPeriod.getDays();
		Period remainPeriod = Period.between(LocalDate.now(), examDueDate.toLocalDateTime().toLocalDate());
		Integer remainDays = remainPeriod.getDays();
		log.info("시험 대비 총 일수 = {}, 오늘 기준 남은 일수 = {}", totalDays, remainDays);
		// 시험 범위 중단원
		Set<String> sectionIdSet = this.examSubSectionIdSet.stream().map(subSection -> subSection.substring(0, 14)).collect(Collectors.toSet());
		log.info("1. 시험범위 중단원들 : " + sectionIdSet.toString());
		// Decide num of trial exam cards
		Integer numTrialExamCards = totalDays <= sectionIdSet.size() + 1 ? 1 : 2;
		log.info("모의고사 카드 개수 = {}", numTrialExamCards);
		// 남은 일수가 적을 때 모의고사 카드 제공
		if(remainDays <= numTrialExamCards) {
			log.info("	남은 일수가 적어서 모의고사 카드 제공");
			String userExamKeyword = getUserExamKeyword(userId);
			log.info("모의고사 진행: " + userExamKeyword);
			cardConfigList.add(CardConfigDTO.builder()
											.cardType(CardConstants.TRIAL_EXAM_CARD_TYPESTR)
											.examKeyword(userExamKeyword)
											.build());
			addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);			
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}
		// FULL_SCOPE_EXAM 카드 제공
		if(totalDays <= sectionIdSet.size()) {
			log.info("	{}일 준비, {}개 중단원 --> FULL_SCOPE_EXAM 카드 제공", totalDays, sectionIdSet.size());
			Integer fullScopeCardProbNum = (int) Math.ceil((double) CardConstants.MAX_CARD_PROB_NUM / sectionIdSet.size());
			for(String sectionId: sectionIdSet) {
				log.info("FULL_SCOPE_EXAM 카드 제공 (중단원: {}, 문제: {}개)", sectionId, fullScopeCardProbNum);
				cardConfigList.add(CardConfigDTO.builder()
												.cardType(CardConstants.FULL_SCOPE_EXAM_CARD_TYPESTR)
												.curriculumId(sectionId)
												.probNum(fullScopeCardProbNum)
												.build());
			}
			addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSectionSet(sectionIdSet));
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}
		// SECTION_EXAM 카드 제공되는 개수
		Integer numSectionExamCards = Math.floorDiv(totalDays - numTrialExamCards, sectionIdSet.size());
		Map<String, Integer> sectionExamCardsNum = new HashMap<String, Integer>();
		sectionIdSet.forEach(sectionId -> sectionExamCardsNum.put(sectionId, numSectionExamCards));
		log.info("2. 시험 대비 카드 중단원 별 SECTION_EXAM 카드 제공 개수 : " + sectionExamCardsNum.toString());
		Map<String, Integer> completedSectionExamCardsNum;
		try {
			completedSectionExamCardsNum = historyManager.getCompletedSectionNum(userId, today, CardConstants.SECTION_EXAM_CARD_TYPESTR + "_question");
		} catch (Exception e) {
			throw e;
		}
		log.info("3. 시험 대비 카드 중단원 별 이미 SECTION_EXAM 카드 푼 개수 : " + completedSectionExamCardsNum.toString());
		for (String sectionId : sectionExamCardsNum.keySet()) {
			if (completedSectionExamCardsNum.containsKey(sectionId)) {
				Integer remainCardCnt = sectionExamCardsNum.get(sectionId) - completedSectionExamCardsNum.get(sectionId);
				if (remainCardCnt <= 0)
					sectionExamCardsNum.remove(sectionId);
				else
					sectionExamCardsNum.put(sectionId, remainCardCnt);
			}
		}
		log.info("4. 시험 대비 카드 중단원 별 앞으로 SECTION_EXAM 카드 제공할 개수 : " + sectionExamCardsNum.toString());
		if (sectionExamCardsNum.keySet().size() != 0) {
			// SECTION_EXAM 카드 제공
			String sectionId = curriculumRepo.sortByCurrSeq(sectionExamCardsNum.keySet()).get(0);
			log.info("SECTION_EXAM 카드 제공 (중단원 : " + sectionId + ")");
			cardConfigList.add(CardConfigDTO.builder()
											.cardType(CardConstants.SECTION_EXAM_CARD_TYPESTR)
											.curriculumId(sectionId)
											.probNum(CardConstants.MAX_CARD_PROB_NUM)
											.build());
			addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}
		// FULL_SCOPE_EXAM 카드 제공
		Integer fullScopeCardProbNum = (int) Math.ceil((double) CardConstants.MAX_CARD_PROB_NUM / sectionIdSet.size());
		for(String sectionId: sectionIdSet) {
			log.info("FULL_SCOPE_EXAM 카드 제공 (중단원: {}, 문제: {}개)", sectionId, fullScopeCardProbNum);
			cardConfigList.add(CardConfigDTO.builder()
											.cardType(CardConstants.FULL_SCOPE_EXAM_CARD_TYPESTR)
											.curriculumId(sectionId)
											.probNum(fullScopeCardProbNum)
											.build());
		}
		addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSectionSet(sectionIdSet));
		
		output.setCardConfigList(cardConfigList);
		output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
		return output;
	}
}
