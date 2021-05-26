package com.tmax.WaplMath.Recommend.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ExamCardProblemKey.class)
@Table(name = "EXAM_CARD_PROBLEM")
public class ExamCardProblem {
	
	@Id	
	private String cardId;
	@Id
	private Integer probId;
	@Id
	private Integer probSequence;
}
