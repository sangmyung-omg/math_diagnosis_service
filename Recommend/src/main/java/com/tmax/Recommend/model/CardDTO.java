package com.tmax.Recommend.model;

import java.util.List;

import lombok.Data;

@Data
public class CardDTO {
	private String cardId;
	private String cardType;
	private String cardTitle;
	private List<ProblemDTO> problemIdList;
	private List<ProblemDTO> cardProbIdList;
}
