package com.tmax.WaplMath.Problem.model;

import java.sql.Timestamp;
import javax.persistence.*;
import com.tmax.WaplMath.Recommend.model.ProblemType;
import lombok.*;

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
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="probId", insertable = false, updatable = false)
	private ProblemImage ProblemImage;
	
	@OneToOne(cascade=(CascadeType.ALL))
	@JoinColumn(name="typeId", insertable = false, updatable = false)
	private ProblemType problemType;

}
