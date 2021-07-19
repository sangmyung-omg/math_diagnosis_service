package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Learning schedule card DTO
 * @author Sangheon Lee
 * @since 2021-06-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDTOV2 {
	private String cardType;
	private String cardTitle;
	private String firstProbLevel;
	private Integer estimatedTime;
	private Float cardScore;
	private String cardDetail;
	private List<ProblemSetListDTO> probIdSetList;

}
