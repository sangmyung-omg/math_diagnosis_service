package com.tmax.WaplMath.Common.util.redis;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class with static methods to help generate id's for redis cache
 */
public class RedisIdGenerator {
    public static String userOrientedID(String domain, String userID, Object ...keyseeds) {
        return domainedID(userID + ":" + domain, keyseeds);
    }

    public static String domainedID(String domain, Object ...keyseeds) {
        List<String> seedList = Arrays.asList(keyseeds)
                                        .stream()
                                        .map(obj -> obj.toString()).collect(Collectors.toList());

        return domain + ":" + String.join(":",seedList);
    }
}
