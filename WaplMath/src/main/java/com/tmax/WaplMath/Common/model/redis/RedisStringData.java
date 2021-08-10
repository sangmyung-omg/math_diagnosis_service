package com.tmax.WaplMath.Common.model.redis;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@RedisHash("StringData")
public class RedisStringData {
    private @Id String id;
    private String data;
    private @TimeToLive long timeout;
}
