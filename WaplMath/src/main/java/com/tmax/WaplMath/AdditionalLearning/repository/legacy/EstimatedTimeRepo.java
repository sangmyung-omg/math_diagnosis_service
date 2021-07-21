package com.tmax.WaplMath.AdditionalLearning.repository.legacy;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.AdditionalLearning.model.UserTargetExamScope;
import com.tmax.WaplMath.Common.model.problem.Problem;

@Repository
public interface EstimatedTimeRepo extends CrudRepository<Problem,String>{

	@Query(value="select p"+
			" from Problem p"+
			" where probId in (:probIdList)")
	List<Problem> getEstimatedTime(@Param("probIdList") List<Integer> probIdList);
}
