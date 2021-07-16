package com.tmax.WaplMath.Common.repository.redis;

import com.tmax.WaplMath.Common.model.redis.RedisStringData;

import org.springframework.data.repository.CrudRepository;

public interface RedisStringRepository extends CrudRepository<RedisStringData, String> {
    
}
