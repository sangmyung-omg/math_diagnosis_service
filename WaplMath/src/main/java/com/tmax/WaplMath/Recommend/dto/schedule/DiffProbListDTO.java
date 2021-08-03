package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Recommend.util.config.CardConstants.Difficulty;
import lombok.Data;

@Data
public class DiffProbListDTO {

	private List<Problem> highProbList;
	private List<Problem> middleProbList;
	private List<Problem> lowProbList;


	public DiffProbListDTO() {
		this.highProbList = new ArrayList<>();
		this.middleProbList = new ArrayList<>();
		this.lowProbList = new ArrayList<>();
	}


	public List<Problem> getDiffProbList(Difficulty difficulty) {
		switch (difficulty.name()) {
			case "상":
				return this.highProbList;
			case "중":
				return this.middleProbList;
			case "하":
				return this.lowProbList;
			default:
				return new ArrayList<>();
		}
	}


	public void addHighProb(Problem highProb) {
		if (this.highProbList == null)
			this.setHighProbList(new ArrayList<>(Arrays.asList(highProb)));
		else
			this.highProbList.add(highProb);
	}


	public void addMiddleProb(Problem middleProb) {
		if (this.middleProbList == null)
			this.setMiddleProbList(new ArrayList<>(Arrays.asList(middleProb)));
		else
			this.middleProbList.add(middleProb);
	}


	public void addLowProb(Problem lowProb) {
		if (this.lowProbList == null)
			this.setLowProbList(new ArrayList<>(Arrays.asList(lowProb)));
		else
			this.lowProbList.add(lowProb);
	}


	public void addDiffProb(Problem prob, Difficulty difficulty) {
		switch (difficulty.name()) {
			case "상":
				this.addHighProb(prob);	break;
			case "중":
				this.addMiddleProb(prob);	break;
			case "하":
				this.addLowProb(prob); break;
			default:
				this.addMiddleProb(prob);	break;
		}
	}
	
  // 문제가 있는 난이도 리스트 리턴
	public List<String> getExistDiffStrList() {
		List<String> diffList = new ArrayList<>();

		for (Difficulty diff : Difficulty.values()) {

			if (!getDiffProbList(diff).isEmpty())
				diffList.add(diff.name());
		}
		return diffList;
	}

  // 문제가 많은 순서대로 난이도 리턴
	public List<Difficulty> getSizeOrderedDiffList() {

		List<Integer> idxList = new ArrayList<>(Arrays.asList(0, 1, 2));

		List<Integer> sizeList = Arrays.asList(this.highProbList, this.middleProbList, this.lowProbList)
																	 .stream().map(list -> list.size()).collect(Collectors.toList());

		Integer maxIdx = sizeList.indexOf(Collections.max(sizeList));
		Integer minIdx = sizeList.indexOf(Collections.min(sizeList));
		minIdx = maxIdx.equals(minIdx) ? 2 : minIdx;

		idxList.remove(maxIdx);
		idxList.remove(minIdx);

		Integer midIdx = idxList.iterator().next();

		return Difficulty.getDiffListByOrder(new Integer[] {maxIdx, midIdx, minIdx});

	}

}
