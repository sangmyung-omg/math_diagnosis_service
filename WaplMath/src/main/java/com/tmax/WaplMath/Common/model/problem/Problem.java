package com.tmax.WaplMath.Common.model.problem;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "PROBLEM")
public class Problem {

	@Id
	private Integer probId;
	private Integer typeId;
	private String answerType;
	private String learningDomain;
	private String question;
	private String solution;
	private String source;
	private Float correctRate;
	private String difficulty;
	private String creatorId;
	private Timestamp createDate;
	private String editorId;
	private Timestamp editDate;
	private String validatorId;
	private Timestamp validateDate;
	private String status;
	private Float timeRecommendation;
	private String frequent;
	private String category;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "typeId", insertable = false, updatable = false)
	private ProblemType problemType;
}
