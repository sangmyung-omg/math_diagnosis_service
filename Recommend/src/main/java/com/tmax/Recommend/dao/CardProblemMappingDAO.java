package com.tmax.Recommend.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CARD_PROBLEM_MAPPING")
public class CardProblemMappingDAO {
	
	@Id
	private String mappingId;
	
	private String cardId;
	private String difficulty;
	private String ukUuid;
	private String isCorrect;
	private Integer probSequence;
}
