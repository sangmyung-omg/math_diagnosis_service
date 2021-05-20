package com.tmax.WaplMath.Recommend.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "USER_EXAM_CURRICULUM_LOG")
@AllArgsConstructor
@NoArgsConstructor
public class UserExamCurriculumLog {
	@Id
	private String cardId;

	private String cardType;
	private String cardTitle;
	private String userUuid;
	private Timestamp recommendedDate;
	private String sectionId;
	private String typeUkUuid;
	private Integer cardSequence;
}
