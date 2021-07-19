package com.tmax.WaplMath.Recommend.service.schedule.v2;

import com.tmax.WaplMath.Recommend.dto.schedule.ScheduleCardOutputDTO;

public interface ScheduleServiceBaseV2 {

	ScheduleCardOutputDTO getExamScheduleCard(String userId);

	ScheduleCardOutputDTO getNormalScheduleCard(String userId);
	
	ScheduleCardOutputDTO getScheduleCardDummy(String userId);

}
