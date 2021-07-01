package com.tmax.WaplMath.Recommend.service.schedule;

import com.tmax.WaplMath.Recommend.dto.schedule.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTOV2;

public interface ScheduleServiceBaseV2 {
	ExamScheduleCardDTO getExamScheduleCard(String userId);
	NormalScheduleCardDTOV2 getNormalScheduleCard(String userId);
	NormalScheduleCardDTOV2 getNormalScheduleCardDummy(String userId);

}
