package com.tmax.WaplMath.Recommend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="CURRICULUM_MASTER")
public class Curriculum {
	
	@Id
	@Column(name="CURRICULUM_ID")
	private String curriculumId;
	
	private String schoolType;
	
	private String grade;
	
	private String semester;
	
	private String chapter;
	
	private String section;
	
	private String subSection;
	
	@Column(name="CHAPTER_ID")
	private String chapterId;
	
	private Integer curriculumSequence;
}
