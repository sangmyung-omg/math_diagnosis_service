package com.tmax.WaplMath.Recommend.model.curriculum;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="ACADEMIC_CALENDAR")
public class AcademicCalendar {
	@Id
	private String curriculumId;
	
	private Integer month;
	private Integer week;
	private String scheduleInfo;
}
