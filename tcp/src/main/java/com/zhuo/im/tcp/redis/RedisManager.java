package com.zhuo.im.tcp.redis;

import com.zhuo.im.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * @description:
 * @version: 1.0
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig config){
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getTcpConfig().getRedis());
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }

}