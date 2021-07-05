package com.tmax.WaplMath.AdditionalLearning.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.AdditionalLearning.model.problem.UserTargetExamScope;

@Repository
public interface UserTargetExamScopeRepo extends CrudRepository<UserTargetExamScope,String>{

	@Query(value="select user_uuid, start_sub_section_id, end_sub_section_id, except_sub_section_id_list"+
			" from user_exam_scope"+
			" where user_uuid = :userId", nativeQuery=true)
	UserTargetExamScope getUserTargetExamScope(@Param("userId") String userId);
}
