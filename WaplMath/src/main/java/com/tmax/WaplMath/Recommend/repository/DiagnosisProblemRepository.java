package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.DiagnosisProblem;

public interface DiagnosisProblemRepository extends CrudRepository<DiagnosisProblem, Integer>{
	
	@Query("SELECT dp FROM DiagnosisProblem dp WHERE SUBSTR(dp.problem.problemType.curriculumId, 0, 11) = ?1 ORDER BY dp.problem.problemType.curriculumId")
	List<DiagnosisProblem> findAllByChapter(String chapter);
}
