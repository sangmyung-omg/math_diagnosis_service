package com.tmax.WaplMath.AdditionalLearning.repository;

import com.tmax.WaplMath.Common.model.user.UserExamScope;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("AddLearn-UserExamScopeRepo")
public interface UserExamScopeRepo extends CrudRepository<UserExamScope, String> {
	@Query("select u from UserExamScope u where u.userUuid = :userId")
    public UserExamScope getExamScopeOfUser(@Param("userId") String userId);
}
