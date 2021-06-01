package com.tmax.WaplMath.Recommend.model.recommend;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.problem.Problem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "EXAM_CARD_PROBLEM")
@IdClass(ExamCardProblemKey.class)
public class ExamCardProblem {

	@Id
	private String cardId;
	@Id
	private Integer probId;
	@Id
	private Integer probSequence;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "cardId", insertable = false, updatable = false)
	private UserExamCard userExamCard;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "probId", insertable = false, updatable = false)
	private Problem problem;
}
