package com.tmax.WaplMath.Recommend.service.schedule;

import com.tmax.WaplMath.Recommend.dto.schedule.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.schedule.NormalScheduleCardDTO;

public interface ScheduleServiceBase {
	ExamScheduleCardDTO getExamScheduleCard(String userId);
	NormalScheduleCardDTO getNormalScheduleCard(String userId);
	NormalScheduleCardDTO getNormalScheduleCardDummy(String userId);
}
