package com.tmax.Recommend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UkRepository extends CrudRepository<UkDAO, String>{
	
	@Query("SELECT ud FROM UkDAO ud WHERE SUBSTR(ud.curriculumId, 1, 11) = ?1 order by ud.curriculumId")
	List<UkDAO> findAllByCurriculumId(String chapterId);
}
