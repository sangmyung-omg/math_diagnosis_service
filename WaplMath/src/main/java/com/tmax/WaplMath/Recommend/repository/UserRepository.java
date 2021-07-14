package com.tmax.WaplMath.Recommend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.tmax.WaplMath.Recommend.model.user.User;

public interface UserRepository extends CrudRepository<User, String> {
	//2021-07-14 Jonghyun Seong
    @Query("select u from User u where u.grade=:grade")
    public List<User> getByGrade(@Param("grade") String grade);
}
