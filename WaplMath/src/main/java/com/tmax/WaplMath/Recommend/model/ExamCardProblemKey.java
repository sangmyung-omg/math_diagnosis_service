package com.tmax.WaplMath.Recommend.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ExamCardProblemKey implements Serializable {
	String cardId;
	Integer probId;
	Integer probSequence;

}
