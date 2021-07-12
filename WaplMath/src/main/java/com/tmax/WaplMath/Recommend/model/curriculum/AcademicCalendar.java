package com.tmax.WaplMath.Recommend.model.curriculum;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
	
	@OneToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "curriculumId", insertable = false, updatable = false)
	private Curriculum curriculum;
}
