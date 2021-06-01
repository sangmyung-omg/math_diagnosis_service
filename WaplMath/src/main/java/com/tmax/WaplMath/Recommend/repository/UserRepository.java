package com.tmax.WaplMath.Recommend.repository;

import org.springframework.data.repository.CrudRepository;

import com.tmax.WaplMath.Recommend.model.user.User;

public interface UserRepository extends CrudRepository<User, String> {
	
}
