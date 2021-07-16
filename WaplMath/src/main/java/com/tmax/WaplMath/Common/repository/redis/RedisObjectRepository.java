package com.tmax.WaplMath.Common.repository.redis;

import com.tmax.WaplMath.Common.model.redis.RedisObjectData;

import org.springframework.data.repository.CrudRepository;

public interface RedisObjectRepository extends CrudRepository<RedisObjectData, String>  {
    
}
