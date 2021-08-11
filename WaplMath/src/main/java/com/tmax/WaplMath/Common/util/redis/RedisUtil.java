package com.tmax.WaplMath.Common.util.redis;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.tmax.WaplMath.Common.model.redis.RedisObjectData;
import com.tmax.WaplMath.Common.model.redis.RedisStringData;
import com.tmax.WaplMath.Common.repository.redis.RedisObjectRepository;
import com.tmax.WaplMath.Common.repository.redis.RedisStringRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisUtil {
    @Autowired
    private RedisObjectRepository redisObjRepo;

    @Autowired
    private RedisStringRepository redisStrRepo;

    @Getter
    private boolean useRedis = false;

    @Autowired
    public RedisUtil(Environment environment, @Value("${spring.redis.host}") String redisHost, @Value("${spring.redis.port}") Integer redisPort){
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        this.useRedis = activeProfiles.contains("useRedis");

        if(this.useRedis){
            log.info("Using Redis server. {}:{}", redisHost, redisPort);
        }
    }


    /**
     * Method to check if given id(key) exists in redis
     * @param id
     * @return
     */
    public boolean hasID(String id){
        if(!useRedis)
            return false;

        Optional<RedisStringData> object =  redisStrRepo.findById(id);
        return object.isPresent();
    }

    /**
     * Save data with expoire time. 
     * @param data base data to save
     * @param expireTime expire time in unit seconds
     * @return
     */
    public int saveWithExpire(RedisObjectData data, long expireTime){
        if(!useRedis)
            return 0;

        data.setTimeout(expireTime);

        //Convert to String data

        try {
            redisObjRepo.save(data);
        }
        catch (IllegalArgumentException e){
            return -1;
        }

        return 0;
    }

    public int saveWithExpire(RedisStringData data, long expireTime){
        if(!useRedis)
            return 0;

        data.setTimeout(expireTime);

        //Convert to String data

        try {
            redisStrRepo.save(data);
        }
        catch (IllegalArgumentException e){
            return -1;
        }

        return 0;
    }

    public RedisStringData get(String key) {
        if(!useRedis)
            return null;

        Optional<RedisStringData> result = redisStrRepo.findById(key);
        if(!result.isPresent())
            return null;

        return result.get();
    }
}
