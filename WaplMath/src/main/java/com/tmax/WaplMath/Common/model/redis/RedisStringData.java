package com.tmax.WaplMath.Common.model.redis;

import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@RedisHash("StringData")
public class RedisStringData {

    @Id
    private String id;

    private String data;
}
