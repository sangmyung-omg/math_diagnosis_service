package com.tmax.WaplMath.Common.model.curriculum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "CURRICULUM_MASTER")
public class Curriculum {

	@Id
	@Column(name = "CURRICULUM_ID")
	private String curriculumId;

	private String schoolType;

	private String grade;

	private String semester;

	private String chapter;

	private String section;

	private String subSection;

	private String part;

	private Integer curriculumSequence;
}
