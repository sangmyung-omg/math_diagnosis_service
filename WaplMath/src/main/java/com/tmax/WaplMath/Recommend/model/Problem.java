package com.tmax.WaplMath.Recommend.model;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="PROBLEM")
public class Problem {
	@Id
	private Integer probId;
	
	private Integer typeId;
	
	private String answerType;
	private String learningDomain;
	private String question;
	private String solution;
	private String source;
	private Integer correctRate;
	private String difficulty;
	private String creatorId;
	private Timestamp createDate;
	private String editorId;
	private Timestamp editDate;
	private String validatorId;
	private Timestamp validateDate;
	private String status;
	private Integer timeRecommendation;
	private String frequent;
	private String category;
	
//	@OneToOne(cascade=(CascadeType.ALL))
//	@JoinColumn(name="chapter", insertable = false, updatable = false)
//	private Curriculum curriculumDao;
	
//	@OneToOne(cascade=(CascadeType.ALL))
//	@JoinColumn(name="ukUuid", insertable = false, updatable = false)
//	private Uk ukDao;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="typeId", insertable = false, updatable = false)
	private ProblemType problemType;
}
