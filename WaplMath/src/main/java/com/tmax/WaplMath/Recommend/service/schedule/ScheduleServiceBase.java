package com.tmax.WaplMath.Recommend.service.schedule;

import com.tmax.WaplMath.Recommend.dto.ExamScheduleCardDTO;

public interface ScheduleServiceBase {
	ExamScheduleCardDTO getExamScheduleCard(String userId);
}
