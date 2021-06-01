package com.tmax.WaplMath.Recommend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.problem.ProblemUkRel;

public interface ProblemUkRelRepository extends CrudRepository<ProblemUkRel, Integer> {
	
	@Query("select ukId from ProblemUkRel PUKR where PUKR.probId = :probId")
	public List<Integer> findAllUkIdByProbId(@Param("probId") Integer probId);
}
