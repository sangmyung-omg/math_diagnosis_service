package com.tmax.WaplMath.AdditionalLearning.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmax.WaplMath.Recommend.model.user.UserExamScope;

@Repository("AddLearn-UserExamScopeRepo")
public interface UserExamScopeRepo extends CrudRepository<UserExamScope, String> {
	@Query("select u from UserExamScope u where u.userUuid = :userId")
    public UserExamScope getExamScopeOfUser(@Param("userId") String userId);
}
