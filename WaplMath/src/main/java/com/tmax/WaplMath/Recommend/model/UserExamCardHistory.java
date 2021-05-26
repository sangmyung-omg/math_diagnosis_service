package com.tmax.WaplMath.Recommend.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "USER_EXAM_CARD_HISTORY")
@AllArgsConstructor
@NoArgsConstructor
public class UserExamCardHistory {
	@Id
	private String cardId;
	
	private String userUuid;
	private String cardType;
	private String cardTitle;
	private String sectionId;
	private String typeId;
	private Integer cardSequence;
}
