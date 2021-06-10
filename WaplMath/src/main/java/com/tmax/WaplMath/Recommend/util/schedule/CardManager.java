package com.tmax.WaplMath.Recommend.util.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepository;

public class CardManager {	
	
	@Autowired
	private ProblemUkRelRepository problemUkRelRepo;
	
	public List<Integer> solvedProbIdList = new ArrayList<Integer>();
	
	public Map<String, List<Problem>> generateDiffProbListByProb(List<Problem> probList) {
		Map<String, List<Problem>> diffProbList = new HashMap<String, List<Problem>>();
		for (Problem prob : probList) {
			String difficulty = prob.getDifficulty();
			if (diffProbList.get(difficulty) == null) {
				List<Problem> tempList = new ArrayList<Problem>();
				tempList.add(prob);
				diffProbList.put(difficulty, tempList);
			} else {
				diffProbList.get(difficulty).add(prob);
			}
		}
		return diffProbList;
	}

	public Map<String, List<Problem>> gerenateDiffProbListByUk(List<Integer> ukList, Integer CARD_UK_NUM) {
		Map<String, List<Problem>> diffProbList = new HashMap<String, List<Problem>>();
		for (Integer ukId : ukList) {
			for (String difficulty : Arrays.asList("상", "중", "하")) {
				List<Problem> probList = new ArrayList<Problem>();
				if (solvedProbIdList.size() != 0)
					probList = problemUkRelRepo.findProbByUkDifficultyNotInList(ukId, difficulty, solvedProbIdList);
				else
					probList = problemUkRelRepo.findProbByUkDifficulty(ukId, difficulty);
				if (probList.size() > 0) {
					Problem prob = probList.get(0);
					if (diffProbList.get(difficulty) == null) {
						List<Problem> tempList = new ArrayList<Problem>();
						tempList.add(prob);
						diffProbList.put(difficulty, tempList);
					} else if (diffProbList.get(difficulty).size() != CARD_UK_NUM) {
						diffProbList.get(difficulty).add(prob);
					}
				}
			}
		}
		return diffProbList;
	}
	
	
}
