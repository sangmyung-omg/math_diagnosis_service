package com.tmax.WaplMath.Common.model.redis;

import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@RedisHash("ObjectData")
public class RedisObjectData {
    private @Id String id;
    private Object data;
    private @TimeToLive long timeout;
}