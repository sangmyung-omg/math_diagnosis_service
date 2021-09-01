package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCardOutputDTO {
	public String message;
	public List<CardDTO> cardList;
}
