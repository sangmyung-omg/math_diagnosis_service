package com.tmax.WaplMath.Problem.model;

import java.sql.Timestamp;
import javax.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name="PROBLEM")
public class Problem {
	
	@Id
	private int probId;
	private int typeId;
	private String answerType;
	private String learningDomain;
	private String question;
	private String solution;
	private String source;
	private float correctRate;
	private String difficulty;
	private String creatorId;
	private Timestamp createDate;
	private String editorId;
	private Timestamp editDate;
	private String validatorId;
	private Timestamp validateDate;
	private String status;
	private float timeRecommendation;
	private String frequent;
	private String category;

}
