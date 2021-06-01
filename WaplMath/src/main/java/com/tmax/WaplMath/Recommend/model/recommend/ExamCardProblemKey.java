package com.tmax.WaplMath.Recommend.model.recommend;

import java.io.Serializable;

import lombok.Data;

@Data
public class ExamCardProblemKey implements Serializable {
	String cardId;
	Integer probId;
	Integer probSequence;

}
