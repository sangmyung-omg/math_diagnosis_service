package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import com.tmax.WaplMath.Common.model.curriculum.AcademicCalendar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("RE-AcademicCalendarRepo")
public interface AcademicCalendarRepo extends CrudRepository<AcademicCalendar, String> {
	@Query("SELECT AC FROM AcademicCalendar AC"
			+ " WHERE SUBSTR(AC.curriculumId,5,1)=?1 AND SUBSTR(AC.curriculumId,7,1)=?2"
			+ " AND (AC.month < ?3 OR (AC.month = ?3 AND AC.week <= ?4))"
			+ " ORDER BY AC.curriculum.curriculumSequence DESC")
	List<AcademicCalendar> findByMonthAndWeek(String grade, String semester, Integer month, Integer week);
}
