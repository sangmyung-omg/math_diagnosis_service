package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.uk.Uk;

public interface UkRepository extends CrudRepository<Uk, Integer>{
	
	@Query("SELECT ud FROM Uk ud WHERE SUBSTR(ud.curriculumId, 1, 11) = ?1 order by ud.curriculumId")
	List<Uk> findAllByCurriculumId(String chapterId);

	//2021-06-25 Jonghyun Seong. Override findAll method to List type return.
	@Override
	List<Uk> findAll();
}
