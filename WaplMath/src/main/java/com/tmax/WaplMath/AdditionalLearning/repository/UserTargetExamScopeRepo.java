package com.tmax.WaplMath.AdditionalLearning.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.AdditionalLearning.model.problem.UserTargetExamScope;

@Repository
public interface UserTargetExamScopeRepo extends CrudRepository<UserTargetExamScope,String>{

	@Query(value="select start_sub_section, end_sub_section"+
			" from user_exam_scope"+
			" where user_uuid = :userId", nativeQuery=true)
	UserTargetExamScope getUserTargetExamScope(@Param("userId") String userId);
}
