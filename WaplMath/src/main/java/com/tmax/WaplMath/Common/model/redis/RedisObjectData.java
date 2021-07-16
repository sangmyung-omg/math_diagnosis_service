package com.tmax.WaplMath.Common.model.redis;

import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@RedisHash("ObjectData")
public class RedisObjectData {
    @Id
    private String id;
    private Object data;
}