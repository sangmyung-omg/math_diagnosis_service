package com.tmax.WaplMath.AnalysisReport.repository.user;

import com.tmax.WaplMath.Common.model.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for user info data with extra conditions
 * @author Jonghyun Seong
 */
@Repository("AR-UserInfoRepo")
public interface UserInfoRepo extends CrudRepository<User, String> {
    @Query("select u from User u where u.userUuid = :userId")
    public User getUserInfoByUUID(@Param("userId") String userId);
}
