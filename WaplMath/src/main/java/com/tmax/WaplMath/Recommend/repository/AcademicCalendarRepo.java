package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.curriculum.AcademicCalendar;

public interface AcademicCalendarRepo extends CrudRepository<AcademicCalendar, String> {
	@Query("SELECT AC FROM AcademicCalendar AC WHERE SUBSTR(AC.curriculumId,5,1)=?1 AND AC.month = ?2 AND AC.week <= ?3 ORDER BY AC.curriculumId DESC")
	List<AcademicCalendar> findByMonthAndWeek(String grade, Integer month, Integer week);
}
