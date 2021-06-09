package com.tmax.WaplMath.AnalysisReport.repository.user;

import com.tmax.WaplMath.Recommend.model.user.UserExamScope;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("AR-UserExamScopeInfoRepo")
public interface UserExamScopeInfoRepo extends CrudRepository<UserExamScope, String> {
    @Query("select userexam from UserExamScope userexam where userexam.userUuid = :userId")
    public UserExamScope getExamScopeOfUser(@Param("userId") String userId);
}
