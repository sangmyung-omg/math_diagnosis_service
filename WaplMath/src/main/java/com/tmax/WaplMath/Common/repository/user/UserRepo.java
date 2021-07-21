package com.tmax.WaplMath.Common.repository.user;

import com.tmax.WaplMath.Common.model.user.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<User, String> {
  
}
