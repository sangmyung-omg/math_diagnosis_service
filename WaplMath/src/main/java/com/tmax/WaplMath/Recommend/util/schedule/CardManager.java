package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tmax.WaplMath.Recommend.dto.CardDTO;
import com.tmax.WaplMath.Recommend.dto.DiffProblemListDTO;
import com.tmax.WaplMath.Recommend.dto.ProblemSetDTO;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.repository.CurriculumRepository;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemTypeRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;

@Component
public class CardManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// Hyperparameter
	private static final Integer MAX_SMALL_CARD_PROBLEM_NUM = 5;
	private static final Integer MAX_EXAM_CARD_PROBLEM_NUM = 15;
	private static final Integer AVERAGE_PROB_ESTIMATED_TIME = 180;

	// Constant
	private static final String TYPE_CARD_TYPE = "type";
	private static final String SUPPLEMENT_CARD_TYPE = "supple";
	private static final String MID_EXAM_CARD_TYPE = "midExam";
	private static final String TRIAL_EXAM_CARD_TYPE = "trialExam";

	@Autowired
	private ProblemRepo problemRepo;
	@Autowired
	private ProblemTypeRepo problemTypeRepo;
	@Autowired
	private CurriculumRepository curriculumRepo;
	@Autowired
	private ProblemUkRelRepository problemUkRelRepo;

	public List<Integer> solvedProbIdList = new ArrayList<Integer>();

	public DiffProblemListDTO generateDiffProbListByProb(List<Problem> probList) {
		DiffProblemListDTO diffProbList = new DiffProblemListDTO();
		for (Problem prob : probList) {
			String difficulty = prob.getDifficulty();
			diffProbList.addDiffProb(prob, difficulty);
		}
		return diffProbList;
	}

	public DiffProblemListDTO gerenateDiffProbListByUk(List<Integer> ukIdList, Integer CARD_UK_NUM) {
		DiffProblemListDTO diffProbList = new DiffProblemListDTO();
		for (Integer ukId : ukIdList) {
			for (String difficulty : Arrays.asList("상", "중", "하")) {
				List<Problem> probList = new ArrayList<Problem>();
				if (solvedProbIdList.size() != 0)
					probList = problemUkRelRepo.findProbByUkDifficultyNotInList(ukId, difficulty, solvedProbIdList);
				else
					probList = problemUkRelRepo.findProbByUkDifficulty(ukId, difficulty);
				if (probList.size() > 0) {
					Problem prob = probList.get(0);
					diffProbList.addDiffProb(prob, difficulty);
				}
			}
		}
		return diffProbList;
	}

	public void printDiffProbList(DiffProblemListDTO diffProbList) {
		logger.info("");
		for (String difficulty : Arrays.asList("상", "중", "하")) {
			List<Problem> probList = diffProbList.getDiffProbList(difficulty);
			List<Integer> probIdList = new ArrayList<Integer>();
			probList.forEach(prob -> probIdList.add(prob.getProbId()));
			logger.info(String.format("%s 난이도 문제들 = " + probIdList.toString(), difficulty));
		}
	}

	public CardDTO addProblemList(CardDTO card, DiffProblemListDTO diffProbList, Integer MAX_PROBLEM_NUM) {
		List<ProblemSetDTO> problemSetList = card.getProbIdSetList();
		Integer estimatedTime = card.getEstimatedTime();
		List<Problem> highProbList, middleProbList, lowProbList;
		middleProbList = diffProbList.getMiddleProbList();
		highProbList = diffProbList.getHighProbList() == null ? middleProbList : diffProbList.getHighProbList();
		lowProbList = diffProbList.getLowProbList() == null ? middleProbList : diffProbList.getLowProbList();

		int probSetCnt = problemSetList.size();
		int listIdx = 0;
		Integer timeTotal = estimatedTime;
		while (true) {
			Problem highProb = highProbList.get(listIdx % highProbList.size());
			Problem middleProb = middleProbList.get(listIdx % middleProbList.size());
			Problem lowProb = lowProbList.get(listIdx % lowProbList.size());

			// add problem set
			problemSetList.add(new ProblemSetDTO(highProb.getProbId(), middleProb.getProbId(), lowProb.getProbId()));
			this.solvedProbIdList.add(highProb.getProbId());
			this.solvedProbIdList.add(middleProb.getProbId());
			this.solvedProbIdList.add(lowProb.getProbId());

			// calc estimated time
			Float highProbTime = highProb.getTimeRecommendation();
			Integer highEstimateTime = (highProbTime == null || highProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME
					: Math.round(highProbTime);
			Float middleProbTime = middleProb.getTimeRecommendation();
			Integer middleEstimateTime = (middleProbTime == null || middleProbTime == 0.0f)
					? AVERAGE_PROB_ESTIMATED_TIME
					: Math.round(middleProbTime);
			Float lowProbTime = lowProb.getTimeRecommendation();
			Integer lowEstimateTime = (lowProbTime == null || lowProbTime == 0.0f) ? AVERAGE_PROB_ESTIMATED_TIME
					: Math.round(lowProbTime);
			timeTotal += Math.round((highEstimateTime + middleEstimateTime + lowEstimateTime) / 3);

			probSetCnt += 1;
			listIdx += 1;
			if (probSetCnt == MAX_PROBLEM_NUM)
				break;
		}
		card.setEstimatedTime(timeTotal);
		card.setProbIdSetList(problemSetList);
		return card;
	}

	public CardDTO generateTypeCard(Integer typeId) {
		CardDTO typeCard = new CardDTO();
		String cardTitle = problemTypeRepo.findTypeNameById(typeId);

		typeCard.setCardType(TYPE_CARD_TYPE);
		typeCard.setCardTitle(cardTitle);
		typeCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		typeCard.setEstimatedTime(0);

		List<Integer> typeProbIdList;
		if (solvedProbIdList.size() != 0)
			typeProbIdList = problemRepo.findAllProbIdByTypeNotInList(typeId, solvedProbIdList);
		else
			typeProbIdList = problemRepo.findAllProbIdByType(typeId);

		if (typeProbIdList.size() == 0)
			return new CardDTO();
		else {
			// type에 대해 triplet 구성
			List<Problem> typeProbList;
			if (solvedProbIdList.size() != 0)
				typeProbList = problemRepo.findAllProbByTypeNotInList(typeId, solvedProbIdList);
			else
				typeProbList = problemRepo.findAllProbByType(typeId);
			DiffProblemListDTO diffProbList = generateDiffProbListByProb(typeProbList);
			printDiffProbList(diffProbList);
			if (diffProbList.getDiffProbList("중") == null)
				return new CardDTO();
			typeCard = addProblemList(typeCard, diffProbList, MAX_SMALL_CARD_PROBLEM_NUM);
			typeCard.setFirstProbLevel("middle");
			return typeCard;
		}
	}

	public CardDTO generateSupplementCard(List<UserKnowledge> lowMasteryList) {
		CardDTO supplementCard = new CardDTO();
		String cardTitle = "";

		supplementCard.setCardType(SUPPLEMENT_CARD_TYPE);
		supplementCard.setCardTitle(cardTitle);
		supplementCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		supplementCard.setEstimatedTime(0);

		List<Integer> lowMasteryUkList = new ArrayList<Integer>();
		lowMasteryList.forEach(uk -> lowMasteryUkList.add(uk.getUkId()));
		DiffProblemListDTO diffProbList = gerenateDiffProbListByUk(lowMasteryUkList, MAX_SMALL_CARD_PROBLEM_NUM);
		supplementCard = addProblemList(supplementCard, diffProbList, MAX_SMALL_CARD_PROBLEM_NUM);
		supplementCard.setFirstProbLevel("low");
		return supplementCard;
	}

	public CardDTO generateMidExamCard(String sectionId) {
		CardDTO midExamCard = new CardDTO();
		String cardTitle = curriculumRepo.findSectionName(sectionId);

		midExamCard.setCardType(MID_EXAM_CARD_TYPE);
		midExamCard.setCardTitle(cardTitle);
		midExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		midExamCard.setEstimatedTime(0);

		// sectionId 내의 모든 문제 id 가져와
		List<Problem> sectionProbList;
		if (solvedProbIdList.size() != 0)
			sectionProbList = problemRepo.findAllProbBySectionNotInList(sectionId, solvedProbIdList);
		else
			sectionProbList = problemRepo.findAllProbBySection(sectionId);
		DiffProblemListDTO diffProbList = generateDiffProbListByProb(sectionProbList);
		midExamCard = addProblemList(midExamCard, diffProbList, MAX_EXAM_CARD_PROBLEM_NUM);
		midExamCard.setFirstProbLevel("middle");
		return midExamCard;
	}

	public CardDTO generateTrialExamCard(List<String> subSectionList) {
		CardDTO trialExamCard = new CardDTO();
		String cardTitle = "";

		trialExamCard.setCardType(TRIAL_EXAM_CARD_TYPE);
		trialExamCard.setCardTitle(cardTitle);
		trialExamCard.setProbIdSetList(new ArrayList<ProblemSetDTO>());
		trialExamCard.setEstimatedTime(0);

		List<Problem> trialExamProbList;
		if (solvedProbIdList.size() != 0)
			trialExamProbList = problemRepo.findAllProbBySubSectionListNotInList(subSectionList, solvedProbIdList);
		else
			trialExamProbList = problemRepo.findAllProbBySubSectionList(subSectionList);
		DiffProblemListDTO diffProbList = generateDiffProbListByProb(trialExamProbList);
		trialExamCard = addProblemList(trialExamCard, diffProbList, MAX_EXAM_CARD_PROBLEM_NUM);
		return trialExamCard;
	}

}
