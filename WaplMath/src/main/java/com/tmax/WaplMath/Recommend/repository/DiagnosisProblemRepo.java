package com.tmax.WaplMath.Recommend.repository;

import java.util.List;
import com.tmax.WaplMath.Common.model.problem.DiagnosisProblem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("RE-DiagnosisProblemRepo")
public interface DiagnosisProblemRepo extends CrudRepository<DiagnosisProblem, Integer>{
	
	@Query("SELECT dp FROM DiagnosisProblem dp WHERE (SUBSTR(dp.basicProblem.problemType.curriculumId, 0, 11) = ?1)"
			+ " AND dp.basicProblem.status = 'ACCEPT'"
			+ " AND dp.upperProblem.status = 'ACCEPT'"
//			+ " AND (dp.lowerProblem.status IS NULL OR dp.lowerProblem.status = 'ACCEPT')"
			+ " AND dp.basicProblem.category = ?2"
			+ " AND dp.upperProblem.category = ?2"
//			+ " AND (dp.lowerProblem.category IS NULL OR dp.lowerProblem.category = ?2)"
			+ " ORDER BY dp.basicProblem.problemType.curriculumId")
	List<DiagnosisProblem> findAllByChapter(String chapter, String diagType);
}
