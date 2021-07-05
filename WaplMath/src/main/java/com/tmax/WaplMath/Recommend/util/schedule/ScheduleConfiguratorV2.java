package com.tmax.WaplMath.Recommend.util.schedule;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Schedule card configuration logic v1
 * @author Sangheon_lee
 * @since 2021-06-30
 */

@Component
public class ScheduleConfiguratorV2 {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// Hyperparameter

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
			logger.info("{}, {}, {}, {}", grade, semester, currentCurriculumId, examType);
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
			logger.info("{}, {}", startSubSectionId, endSubSectionId);
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
		List<String> sourceTypeList = new ArrayList<String>(
			Arrays.asList("type_question", "supple_question", "mid_exam_question", "trial_exam_question"));
		try {
			this.solvedProbIdSet = historyManager.getSolvedProbIdSet(userId, today, "", sourceTypeList);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void setExamSubSectionIdSet(UserExamScope userExamScopeInfo) {
		List<String> examSubSectionIdList=null;
		String startSubSectionId = userExamScopeInfo.getStartSubSectionId();
		String endSubSectionId = userExamScopeInfo.getEndSubSectionId();
		String exceptSubSectionIdStr = userExamScopeInfo.getExceptSubSectionIdList();
		List<String> exceptSubSectionIdList;
		if(exceptSubSectionIdStr != null) {
			exceptSubSectionIdList = Arrays.asList(exceptSubSectionIdStr.split(", "));
			logger.info("{}", exceptSubSectionIdList);
			examSubSectionIdList = curriculumRepo.findSubSectionListBetweenExcept(startSubSectionId, endSubSectionId, exceptSubSectionIdList); // 이번 학기 마지막까지
		}else {
			examSubSectionIdList = curriculumRepo.findSubSectionListBetween(startSubSectionId, endSubSectionId); // 이번 학기 마지막까지
		}
		this.examSubSectionIdSet = new HashSet<String>();
		this.examSubSectionIdSet.addAll(examSubSectionIdList);
		logger.info("시험 소단원 범위 = {}", examSubSectionIdSet);
	}
	
	public List<String> getNormalSubSectionList(String userId) throws Exception {
		List<String> subSectionIdList;
		User userInfo = getValidUserInfo(userId);
		String endCurriculumId = ExamScope.examScope.get(userInfo.getGrade() + "-" + userInfo.getSemester() + "-" + "final").get(1);
		subSectionIdList = curriculumRepo.findSubSectionListBetween(userInfo.getCurrentCurriculumId(), endCurriculumId); // 이번 학기 마지막까지
		logger.info("전체 소단원 범위 = {}", subSectionIdList);
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
		logger.info("전체 소단원 범위 = {}", subSectionIdList);
		// 중간평가 판단 - 중단원만 일단
		Set<String> sectionIdSet = new HashSet<String>(); // 토탈 범위 중 단원들
		subSectionIdList.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));
		logger.info("1.지금부터 중 단원들 : " + sectionIdSet.toString());
		//		Set<String> sectionSet = new HashSet<String>(Arrays.asList("중등-중2-1학-01-02"));

		List<Integer> completedTypeIdList;
		try {
			completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", "type_question");
		} catch (Exception e) {
			throw e;
		}
		logger.info("2. 유저가 푼 유형 UK들 : " + completedTypeIdList.toString());

		List<ProblemType> remainTypeList = problemTypeRepo.NfindRemainTypeIdList(subSectionIdList, completedTypeIdList);
		Set<String> notDoneSectionIdSet = new HashSet<String>(); // 안푼 중단원들
		if (completedTypeIdList.size() == 0) {
			logger.info("- 푼게 하나도 없음.");
			notDoneSectionIdSet.addAll(sectionIdSet);
		} else {
			remainTypeList.forEach(type -> notDoneSectionIdSet.add(type.getCurriculumId().substring(0, 14)));
		}
		logger.info("3. 안푼 중 단원들 : " + notDoneSectionIdSet.toString());

		sectionIdSet.removeAll(notDoneSectionIdSet); // sectionSet에 완벽히 푼 단원들 저장됨
		logger.info("4. 완벽히 유형카드를 푼 중 단원들 : " + sectionIdSet.toString());

		List<String> completedSectionIdList;
		try {
			completedSectionIdList = historyManager.getCompletedSectionIdList(userId, today, "mid_exam_question");
		} catch (Exception e) {
			throw e;
		}
		Set<String> completedSectionIdSet = new HashSet<String>(completedSectionIdList);
		logger.info("5. 이미 중간평가 카드 푼 중 단원들 : " + completedSectionIdSet.toString());

		sectionIdSet.removeAll(completedSectionIdSet); // sectionSet에 완벽히 푼 단원들 - 이미 푼 중간평가 저장됨
		logger.info("6. 중간평가 대상인 최종 중 단원 : " + sectionIdSet.toString());

		// 완벽히 푼 단원이 있으면 중간 평가
		if (sectionIdSet.size() != 0) {
			String sectionId = sectionIdSet.iterator().next();
			logger.info("중간에 다 풀었으니까 중간평가 진행: " + sectionId);
			cardConfigList.add(CardConfigDTO.builder()
											.cardType(CardConstants.SECTION_MID_EXAM_CARD_TYPESTR)
											.midExamCurriculumId(sectionId)
											.midExamType("section")
											.build());
			addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}

		// 보충 필요한지 판단
		List<Integer> suppleTypeIdList;
		try {
			suppleTypeIdList = historyManager.getCompletedTypeIdList(userId, today, "", "supple_question");
		} catch (Exception e) {
			throw e;
		}
		logger.info("7. 지금까지 보충 카드로 풀어본 유형ID 리스트 = ");
		logger.info(suppleTypeIdList.toString());

		List<Integer> solvedTypeIdList;
		try {
			solvedTypeIdList = historyManager.getCompletedTypeIdListAfterSuppleCard(userId, today);
		} catch (Exception e) {
			throw e;
		}
		solvedTypeIdList.removeAll(suppleTypeIdList);
		if (solvedTypeIdList.size() != 0) {
			List<TypeMasteryDTO> lowTypeMasteryList = userKnowledgeRepo.findNLowTypeMasteryList(userId, solvedTypeIdList, CardConstants.LOW_MASTERY_THRESHOLD);
			logger.info("8. 보충 카드로 풀어본 유형 제외, 낮은 유형 카드들의 이해도 리스트 = ");
			for (TypeMasteryDTO typeMastery : lowTypeMasteryList)
				logger.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));

			// 보충 채울만큼 넉넉하면 보충 카드
			if (lowTypeMasteryList.size() >= CardConstants.SUPPLE_CARD_TYPE_NUM) {
				logger.info("	이해도 낮은게 많아서 보충 카드 진행");
				List<TypeMasteryDTO> typeMasteryList = lowTypeMasteryList.subList(0, CardConstants.SUPPLE_CARD_TYPE_NUM);
				List<Integer> suppleCardTypeIdList = new ArrayList<Integer>();
				for (TypeMasteryDTO e : typeMasteryList)
					suppleCardTypeIdList.add(e.getTypeId());
				cardConfigList.add(CardConfigDTO.builder()
									  			.cardType(CardConstants.SUPPLE_CARD_TYPESTR)
									  			.typeMasteryList(typeMasteryList)
									  			.build());
				addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));
				totalCardProbNum += CardConstants.SUPPLE_CARD_TYPE_NUM * CardConstants.SUPPLE_CARD_PROB_NUM_PER_TYPE;

				if (lowTypeMasteryList.size() >= CardConstants.SUPPLE_CARD_TYPE_NUM * 2) {
					logger.info("	보충카드 하나 더 추가");
					typeMasteryList = lowTypeMasteryList.subList(CardConstants.SUPPLE_CARD_TYPE_NUM, CardConstants.SUPPLE_CARD_TYPE_NUM * 2);
					suppleCardTypeIdList = new ArrayList<Integer>();
					for (TypeMasteryDTO e : typeMasteryList)
						suppleCardTypeIdList.add(e.getTypeId());
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
				logger.info("	{}개 이상 잘하고 있으니 보충 카드 진행", CardConstants.SUPPLE_CARD_TYPE_THRESHOLD);
				List<TypeMasteryDTO> typeMasteryList = userKnowledgeRepo.findBottomTypeMasteryList(userId, solvedTypeIdList, CardConstants.SUPPLE_CARD_TYPE_NUM);
				logger.info("	유형 카드들의 이해도 리스트 = ");
				for (TypeMasteryDTO typeMastery : typeMasteryList)
					logger.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));
				List<Integer> suppleCardTypeIdList = new ArrayList<Integer>();
				for (TypeMasteryDTO e : typeMasteryList)
					suppleCardTypeIdList.add(e.getTypeId());
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
				logger.info("중간평가 아니니까 유형 UK 카드 진행: " + typeId);
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
			logger.info("문제가 없어서 못만든 유형UK: " + noProbTypeIdList);
		}
		// 유형도 다풀었는데 문제가 안채워지면 추가 보충카드
		if (totalCardProbNum < CardConstants.MAX_CARD_PROB_NUM) {
			String examKeyword = getUserExamKeyword(userId);
			String examStartSubSectionId = ExamScope.examScope.get(examKeyword).get(0);
			String examEndSubSectionId = ExamScope.examScope.get(examKeyword).get(1);
			Integer addtiTypeNum = (int) Math.ceil((CardConstants.MAX_CARD_PROB_NUM - totalCardProbNum) / 2.0);
			List<TypeMasteryDTO> addtiTypeMasteryList = userKnowledgeRepo.findTypeMasteryListBetween(userId, examStartSubSectionId, examEndSubSectionId)
																		 .subList(0, addtiTypeNum);			
			List<Integer> addtlSuppleCardTypeIdList = new ArrayList<Integer>();
			for (TypeMasteryDTO e : addtiTypeMasteryList)
				addtlSuppleCardTypeIdList.add(e.getTypeId());
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
		logger.info("중간평가 아니니까 유형 UK 카드 진행: " + typeId);
		cardConfigList.add(CardConfigDTO.builder()
										.cardType(CardConstants.TYPE_CARD_TYPESTR)
										.typeId(typeId)
										.build());
		addtlSubSectionIdSet.add(problemTypeRepo.findById(typeId).orElse(new ProblemType()).getCurriculumId());

		// 보충 카드 : 둘, 셋, 네번째 유형에 대해
		List<Integer> suppleTypeIdList = new ArrayList<Integer>();
		List<Integer> solvedTypeIdList = new ArrayList<Integer>(
			Arrays.asList(remainTypeList.get(1).getTypeId(), remainTypeList.get(2).getTypeId(), remainTypeList.get(3).getTypeId()));
		List<TypeMasteryDTO> lowMasteryTypeList = userKnowledgeRepo.findLowTypeMasteryList(userId, solvedTypeIdList, suppleTypeIdList,
			CardConstants.LOW_MASTERY_THRESHOLD);
		logger.info("8. 보충 카드로 풀어본 유형 제외, 유형 카드들의 이해도 리스트 = ");
		for (TypeMasteryDTO typeMastery : lowMasteryTypeList)
			logger.info(String.format("   typeId = %s, mastery = %f", typeMastery.getTypeId(), typeMastery.getMastery()));
		logger.info("	이해도 낮은게 많아서 보충 카드 진행");
		List<Integer> suppleCardTypeIdList = new ArrayList<Integer>();
		List<TypeMasteryDTO> typeMasteryList = lowMasteryTypeList.subList(0, CardConstants.SUPPLE_CARD_TYPE_NUM);
		for (TypeMasteryDTO e : typeMasteryList)
			suppleCardTypeIdList.add(e.getTypeId());
		cardConfigList.add(CardConfigDTO.builder()
							  			.cardType(CardConstants.SUPPLE_CARD_TYPESTR)
							  			.typeMasteryList(typeMasteryList)
							  			.build());
		addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(suppleCardTypeIdList));

		// 중간 평가 카드 (중단원)
		String sectionId = currentCurriculumId.substring(0, 14);
		logger.info("중간평가 진행(중단원): " + sectionId);
		cardConfigList.add(CardConfigDTO.builder()
										.cardType(CardConstants.SECTION_MID_EXAM_CARD_TYPESTR)
										.midExamCurriculumId(sectionId)
										.midExamType("section")
										.build());
		addtlSubSectionIdSet.addAll(curriculumRepo.findSubSectionListInSection(sectionId));

		// 추가 보충 카드
		String startSubSectionId = userExamScopeInfo.getStartSubSectionId();
		String endSubSectionId = userExamScopeInfo.getEndSubSectionId();
		Integer addtiTypeNum = (int) Math.ceil((CardConstants.MAX_CARD_PROB_NUM - 4) / 2.0);
		List<TypeMasteryDTO> addtiTypeMasteryList = userKnowledgeRepo.findTypeMasteryListBetween(userId, startSubSectionId, endSubSectionId)
																	 .subList(0, addtiTypeNum);			
		List<Integer> addtlSuppleCardTypeIdList = new ArrayList<Integer>();
		for (TypeMasteryDTO e : addtiTypeMasteryList)
			addtlSuppleCardTypeIdList.add(e.getTypeId());
		cardConfigList.add(CardConfigDTO.builder()
										.cardType(CardConstants.ADDTL_SUPPLE_CARD_TYPESTR)
										.typeMasteryList(addtiTypeMasteryList)
										.build());
		addtlSubSectionIdSet.addAll(problemTypeRepo.findSubSectionListInTypeList(addtlSuppleCardTypeIdList));
				
		// 모의고사 카드
		String userExamKeyword = getUserExamKeyword(userId);
		logger.info("모의고사 진행: " + userExamKeyword);
		cardConfigList.add(CardConfigDTO.builder()
										.cardType(CardConstants.TRIAL_EXAM_CARD_TYPESTR)
										.trialExamType(userExamKeyword)
										.build());
		addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);

		output.setCardConfigList(cardConfigList);
		output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
		return output;
	}

	public ScheduleConfigDTO getExamScheduleConfig(String userId) throws Exception {
		ScheduleConfigDTO output = new ScheduleConfigDTO();
		List<CardConfigDTO> cardConfigList = new ArrayList<CardConfigDTO>();
		Set<String> addtlSubSectionIdSet = new HashSet<String>();
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
		logger.info("시험 대비 총 일수 = {}, 오늘 기준 남은 일수 = {}", totalDays, remainDays);
		// Decide num of trial exam cards
		Set<String> sectionIdSet = new HashSet<String>();
		this.examSubSectionIdSet.forEach(subSection -> sectionIdSet.add(subSection.substring(0, 14)));
		logger.info("1.시험범위 중단원들 : " + sectionIdSet.toString());
		Integer numTrialExamCards = totalDays + 1 <= sectionIdSet.size() ? 1 : 2;
		// 남은 일수가 적을 때 모의고사 카드 제공
		if(remainDays <= numTrialExamCards) {
			// 모의고사 카드
			String userExamKeyword = getUserExamKeyword(userId);
			logger.info("모의고사 진행: " + userExamKeyword);
			cardConfigList.add(CardConfigDTO.builder()
											.cardType(CardConstants.TRIAL_EXAM_CARD_TYPESTR)
											.trialExamType(userExamKeyword)
											.build());
			addtlSubSectionIdSet.addAll(this.examSubSectionIdSet);			
			output.setCardConfigList(cardConfigList);
			output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
			return output;
		}
		// type1 카드 제공되는 개수
		Integer numExamType1Cards = Math.floorDiv(totalDays - numTrialExamCards, sectionIdSet.size());
		Map<String, Integer> sectionCompleteCnt;
		try {
			sectionCompleteCnt = historyManager.getSectionCompletedCnt(userId, today, "exam_type1_question");
		} catch (Exception e) {
			throw e;
		}
		logger.info("2. 이미 시험대비 카드 type1 푼 중단원들 (횟수) : " + sectionCompleteCnt.toString());
		
		
		
		
		output.setCardConfigList(cardConfigList);
		output.setAddtlSubSectionIdSet(addtlSubSectionIdSet);
		return output;
	}
}
