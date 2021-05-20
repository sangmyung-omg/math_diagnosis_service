package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.Uk;

public interface UkRepository extends CrudRepository<Uk, String>{
	
	@Query("SELECT ud FROM UkDAO ud WHERE SUBSTR(ud.curriculumId, 1, 11) = ?1 order by ud.curriculumId")
	List<Uk> findAllByCurriculumId(String chapterId);
}
