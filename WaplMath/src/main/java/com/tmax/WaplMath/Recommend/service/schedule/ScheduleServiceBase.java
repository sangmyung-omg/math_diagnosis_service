package com.tmax.WaplMath.Recommend.service.schedule;

import com.tmax.WaplMath.Recommend.dto.ExamScheduleCardDTO;
import com.tmax.WaplMath.Recommend.dto.NormalScheduleCardDTO;

public interface ScheduleServiceBase {
	ExamScheduleCardDTO getExamScheduleCard(String userId);
	NormalScheduleCardDTO getNormalScheduleCard(String userId);
}
