package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tmax.WaplMath.Recommend.model.problem.Problem;

import lombok.Data;

@Data
public class DiffProbListDTO {
	public List<Problem> highProbList;
	public List<Problem> middleProbList;
	public List<Problem> lowProbList;

	public DiffProbListDTO() {
		this.highProbList = new ArrayList<Problem>();
		this.middleProbList = new ArrayList<Problem>();
		this.lowProbList = new ArrayList<Problem>();
	}

	public List<Problem> getDiffProbList(String difficulty) {
		switch (difficulty) {
		case "상":
			return this.highProbList;
		case "중":
			return this.middleProbList;
		case "하":
			return this.lowProbList;
		default:
			return new ArrayList<Problem>();
		}
	}

	public void addHighProb(Problem highProb) {
		if (this.highProbList == null)
			this.setHighProbList(new ArrayList<Problem>(Arrays.asList(highProb)));
		else
			this.highProbList.add(highProb);
	}

	public void addMiddleProb(Problem middleProb) {
		if (this.middleProbList == null)
			this.setMiddleProbList(new ArrayList<Problem>(Arrays.asList(middleProb)));
		else
			this.middleProbList.add(middleProb);
	}

	public void addLowProb(Problem lowProb) {
		if (this.lowProbList == null)
			this.setLowProbList(new ArrayList<Problem>(Arrays.asList(lowProb)));
		else
			this.lowProbList.add(lowProb);
	}

	public void addDiffProb(Problem prob, String difficulty) {
		switch (difficulty) {
		case "상":
			this.addHighProb(prob);
			break;
		case "중":
			this.addMiddleProb(prob);
			break;
		case "하":
			this.addLowProb(prob);
			break;
		default:
			this.addMiddleProb(prob);
			break;
		}
	}
}
