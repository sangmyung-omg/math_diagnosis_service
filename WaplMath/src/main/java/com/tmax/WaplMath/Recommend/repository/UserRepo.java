package com.tmax.WaplMath.Recommend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.tmax.WaplMath.Common.model.user.User;

@Repository("RE-UserRepo")
public interface UserRepo extends CrudRepository<User, String> {
	//2021-07-14 Jonghyun Seong
    @Query("select u from User u where u.grade=:grade")
    public List<User> getByGrade(@Param("grade") String grade);
}
