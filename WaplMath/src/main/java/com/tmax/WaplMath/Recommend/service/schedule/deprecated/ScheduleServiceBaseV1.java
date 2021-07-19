package com.tmax.WaplMath.Recommend.service.schedule.deprecated;

import com.tmax.WaplMath.Recommend.dto.schedule.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTOV1;

public interface ScheduleServiceBaseV1 {
	ExamScheduleCardDTO getExamScheduleCard(String userId);
	NormalScheduleCardDTOV1 getNormalScheduleCard(String userId);
	NormalScheduleCardDTOV1 getNormalScheduleCardDummy(String userId);
}
