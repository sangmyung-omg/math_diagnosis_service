package com.tmax.WaplMath.Common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableRedisRepositories(basePackages = {"com.tmax.WaplMath.Common.repository.redis"})
@Configuration
public class RedisConfig {

    @Getter
    @Value("${spring.redis.host}")
    private String redisHost;

    @Getter
    @Value("${spring.redis.port}")
    private int redisPort;

    //Check if redis is set correctly
    public boolean isSet(){
        return redisHost != null;
    }
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
