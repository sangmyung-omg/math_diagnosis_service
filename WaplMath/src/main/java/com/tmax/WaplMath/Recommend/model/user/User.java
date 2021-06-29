package com.tmax.WaplMath.Recommend.model.user;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;

import lombok.Data;

@Data
@Entity
@Table(name = "USER_MASTER")
public class User {
	@Id
	private String userUuid;

	private String grade;
	private String semester;
	private String name;
	private String currentCurriculumId;

	@Column(name = "EXAM_TYPE")
	private String examType;

	@Column(name = "EXAM_START_DATE")
	private Timestamp examStartDate;

	@Column(name = "EXAM_DUE_DATE")
	private Timestamp examDueDate;

	@Column(name = "EXAM_TARGET_SCORE")
	private Integer examTargetScore;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "currentCurriculumId", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum currentCurriculum;
}
