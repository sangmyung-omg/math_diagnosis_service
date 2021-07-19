package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.tmax.WaplMath.Recommend.util.config.CardConstants.Difficulty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemSetListDTO {

	private Integer min;

	private Integer max;

	@Builder.Default private List<Integer> high = new ArrayList<>();

	@Builder.Default private List<Integer> middle = new ArrayList<>();

	@Builder.Default private List<Integer> low = new ArrayList<>();


	public List<Integer> getDiffProbIdList(Difficulty diff) {
		switch (diff.name()) {
			case "상":
				return this.high;
			case "중":
				return this.middle;
			case "하":
				return this.low;
			default:
				return new ArrayList<>();
		}
	}


	public void addHighProbId(Integer highProbId) {
		if (this.high == null)
			this.setHigh(new ArrayList<>(Arrays.asList(highProbId)));
		else
			this.high.add(highProbId);
	}


	public void addMiddleProbId(Integer middleProbId) {
		if (this.middle == null)
			this.setMiddle(new ArrayList<>(Arrays.asList(middleProbId)));
		else
			this.middle.add(middleProbId);
	}


	public void addLowProbId(Integer lowProbId) {
		if (this.low == null)
			this.setLow(new ArrayList<>(Arrays.asList(lowProbId)));
		else
			this.low.add(lowProbId);
	}


	public void addDiffProb(Integer probId, Difficulty diff) {
		switch (diff.name()) {
			case "상":
				this.addHighProbId(probId);	break;
			case "중":
				this.addMiddleProbId(probId);	break;
			case "하":
				this.addLowProbId(probId);	break;
			default:
				this.addMiddleProbId(probId);	break;
		}
	}
}
