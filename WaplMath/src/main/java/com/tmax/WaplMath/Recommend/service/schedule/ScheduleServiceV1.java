package com.tmax.WaplMath.Recommend.service.schedule;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tmax.WaplMath.Recommend.dto.schedule.CardConfigDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.CardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleConfigDTO;
import com.tmax.WaplMath.Recommend.util.schedule.CardGenerator;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleConfigurator;
import com.tmax.WaplMath.Recommend.util.schedule.ScheduleHistoryManagerV1;

/**
 * Generate today normal/exam schedule card
 * @author Sangheon_lee
 */
@Service("ScheduleServiceV1")
public class ScheduleServiceV1 implements ScheduleServiceBase {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	CardGenerator cardGenerator = new CardGenerator();
	@Autowired
	ScheduleConfigurator scheduleConfigurator = new ScheduleConfigurator();
	@Autowired
	ScheduleHistoryManagerV1 historyManager;

	@Override
	public NormalScheduleCardDTO getNormalScheduleCard(String userId) {
		NormalScheduleCardDTO output = new NormalScheduleCardDTO();
		List<CardDTO> cardList = new ArrayList<CardDTO>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getNormalScheduleConfig(userId);
		} catch (Exception e) {
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		CardDTO card;
		logger.info("소단원: {}", scheduleConfig.getAddtlSubSectionIdSet());
		for (CardConfigDTO cardConfig : scheduleConfig.getCardConfigList()) {
			card = cardGenerator.generateCard(cardConfig);
			cardList.add(card);
		}
		output.setCardList(cardList);
		output.setMessage("Successfully return curriculum card list.");
		return output;
	}

	@Override
	// set to dummy --> 4개 카드 종류별로 return
	public NormalScheduleCardDTO getNormalScheduleCardDummy(String userId) {
		NormalScheduleCardDTO output = new NormalScheduleCardDTO();
		List<CardDTO> cardList = new ArrayList<CardDTO>();
		ScheduleConfigDTO scheduleConfig;
		try {
			scheduleConfig = scheduleConfigurator.getDummyScheduleConfig(userId);
		} catch (Exception e) {
			output.setMessage("schedule configuration failure. " + e.getMessage());
			return output;
		}
		cardGenerator.userId = userId;
		cardGenerator.setSolvedProbIdSet(scheduleConfigurator.getSolvedProbIdSet());
		cardGenerator.setExamSubSectionIdSet(scheduleConfigurator.getExamSubSectionIdSet());
		CardDTO card;
		logger.info("소단원: {}", scheduleConfig.getAddtlSubSectionIdSet());
		for (CardConfigDTO cardConfig : scheduleConfig.getCardConfigList()) {
			card = cardGenerator.generateCard(cardConfig);
			cardList.add(card);
		}
		output.setCardList(cardList);
		output.setMessage("Successfully return curriculum card list.");
		return output;

	}

	@Override
	public ExamScheduleCardDTO getExamScheduleCard(String userId) {
		ExamScheduleCardDTO output = new ExamScheduleCardDTO();
		return output;
		/*
		List<CardDTO> cardList = new ArrayList<CardDTO>();
		
		// Timestamp todayTimestamp = Timestamp.valueOf(LocalDate.now().atStartOfDay());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String today = LocalDate.now().format(formatter);
		
		this.userId = userId;
		this.today = today;
		List<String> sourceTypeList = new ArrayList<String>(
				Arrays.asList("type_question", "supple_question", "mid_exam_question", "trial_exam_question"));
		try {
			this.solvedProbIdList = historyManager.getCompletedProbIdList(userId, today, sourceTypeList);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		logger.info("\n이미 푼 probId 리스트 : " + solvedProbIdList);
		
		// Load user information from USER_MASTER TB
		User userInfo;
		try {
			userInfo = userRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			output.setMessage(String.format("userId = %s is not in USER_MASTER TB.", e.getMessage()));
			return output;
		}
		
		// Check whether user exam information is null
		String examType = userInfo.getExamType();
		Timestamp examStartDate = userInfo.getExamStartDate();
		Timestamp examDueDate = userInfo.getExamDueDate();
		Integer examTargetScore = userInfo.getExamTargetScore();
		if (examType == null || examStartDate == null || examDueDate == null || examTargetScore == null) {
			output.setMessage("One of user's exam infomation is null. Call ExamInfo PUT service first.");
			return output;
		}
		
		UserExamScope userExamScope;
		try {
			userExamScope = userExamScopeRepo.findById(userId).orElseThrow(() -> new NoSuchElementException(userId));
		} catch (NoSuchElementException e) {
			output.setMessage(String.format("userId = %s is not in USER_EXAM_SCOPE TB.", e.getMessage()));
			return output;
		}
		Integer startSeq = userExamScope.getStartCurriculum().getCurriculumSequence();
		Integer endSeq = userExamScope.getEndCurriculum().getCurriculumSequence();
		List<String> subSectionList = curriculumRepo.findSubSectionListBySeq(startSeq, endSeq); // 시험범위
		
		List<String> totalSectionList = problemTypeRepo.findAllSection(subSectionList);
		Set<String> totalSectionSet = new HashSet<String>(totalSectionList); // 토탈 범위 중 단원들
		logger.info("1. 시험범위 전체 중 단원들 : " + totalSectionSet.toString());
		
		List<Integer> completedTypeIdList;
		try {
			completedTypeIdList = historyManager.getCompletedTypeIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		logger.info("2. 유저가 푼 유형 UK들 : " + completedTypeIdList.toString());
		
		List<String> notCompletedSectionList = new ArrayList<String>();
		if (completedTypeIdList.size() == 0) {
			logger.info("- 푼게 하나도 없음.");
			notCompletedSectionList.addAll(totalSectionList);
		} else
			notCompletedSectionList = problemTypeRepo.findAllSectionNotInTypeList(subSectionList, completedTypeIdList);
		Set<String> notCompletedSectionSet = new HashSet<String>(notCompletedSectionList); // 안푼 단원들
		logger.info("3. 안푼 중단원들 : " + notCompletedSectionSet.toString());
		
		totalSectionSet.removeAll(notCompletedSectionSet); // totalSectionSet에 완벽히 푼 단원들 저장됨
		logger.info("4. 완벽히 유형카드를 중 푼 단원들 : " + totalSectionSet.toString());
		
		List<String> completedSectionIdList;
		try {
			completedSectionIdList = historyManager.getCompletedSectionIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		Set<String> completedSectionSet = new HashSet<String>(completedSectionIdList);
		logger.info("5. 이미 중간평가 카드 푼 중 단원들 : " + completedSectionSet.toString());
		
		totalSectionSet.removeAll(completedSectionSet); // totalSectionSet에 완벽히 푼 단원들 - 이미 푼 중간평가 저장됨
		logger.info("6. 중간평가 대상인 최종 중 단원 : " + totalSectionSet.toString());
		
		// 완벽히 푼 단원이 있으면 중간 평가
		if (totalSectionSet.size() != 0) {
			String sectionId = totalSectionSet.iterator().next();
			logger.info("\n중간에 다 풀었으니까 중간평가 진행: " + sectionId);
			CardDTO midExamCard = cardManager.generateMidExamCard(sectionId);
			cardList.add(midExamCard);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		}
		
		// 유형 카드 (혹은 보충 UK)
		// 보충 필요한지 판단
		List<Integer> suppleUkIdList;
		try {
			suppleUkIdList = historyManager.getCompletedSuppleUkIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		logger.info("7. 지금까지 보충 카드로 풀어본 ukID 리스트 = ");
		logger.info(suppleUkIdList.toString());
		
		List<Integer> solvedUkIdList;
		try {
			solvedUkIdList = historyManager.getSolvedUkIdList(userId, today);
		} catch (Exception e) {
			output.setMessage(e.getMessage());
			return output;
		}
		List<UserKnowledge> lowMasteryList = userKnowledgeRepo.findAllLowMasteryUkUuid(userId, solvedUkIdList,
				suppleUkIdList, SUP_UK_MASTERY_THRESHOLD);
		
		logger.info("8. 보충 카드 제외, 유형 카드로 추천된 ukID의 이해도 리스트 = ");
		for (UserKnowledge lowMastery : lowMasteryList)
			logger.info(String.format("   ukId = %s, mastery = %f", lowMastery.getUkId(), lowMastery.getUkMastery()));
		
		// 보충 채울만큼 넉넉하면 보충 카드
		if (lowMasteryList.size() >= MAX_SMALL_CARD_PROBLEM_NUM) {
			logger.info("	이해도 낮은게 많아서 보충 카드 진행");
			CardDTO supplementCard = cardManager.generateSupplementCard(lowMasteryList);
			cardList.add(supplementCard);
		}
		
		// 나머지 카드들 유형카드로 채우기
		List<Integer> remainTypeIdList;
		if (completedTypeIdList.size() == 0)
			remainTypeIdList = problemTypeRepo.findAllExamTypeIdList(subSectionList);
		else
			remainTypeIdList = problemTypeRepo.findRemainTypeIdList(subSectionList, completedTypeIdList);
		
		List<Integer> noProbTypeList = new ArrayList<Integer>();
		// 공부 안한 유형uk가 있으면 유형카드
		if (remainTypeIdList.size() != 0) {
			for (Integer typeId : remainTypeIdList) {
				logger.info("\n중간평가 아니니까 유형 UK 카드 진행: " + typeId);
				CardDTO typeCard;
				typeCard = cardManager.generateTypeCard(typeId);
				// 문제가 하나도 없으면 뛰어넘자
				if (typeCard.getCardType() == null) {
					noProbTypeList.add(typeId);
					continue;
				}
				cardList.add(typeCard);
				if (cardList.size() == MAX_CARD_NUM)
					break;
			}
			logger.info("문제가 없어서 못만든 유형UK: " + noProbTypeList);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		}
		
		// 보충카드 한장만 구성된 경우, 한장만 제공
		if (cardList.size() != 0) {
			logger.info("문제가 없어서 못만든 유형UK: " + noProbTypeList);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		} else {
			logger.info("	다 풀어서 모의고사 카드 진행. ");
			CardDTO trialExamCard = cardManager.generateTrialExamCard(subSectionList);
			cardList.add(trialExamCard);
			output.setCardList(cardList);
			output.setMessage("Successfully return curriculum card list.");
			return output;
		}
		*/
	}

}
