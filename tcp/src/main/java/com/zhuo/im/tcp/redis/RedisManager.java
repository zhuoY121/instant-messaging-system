package com.zhuo.im.tcp.redis;

import com.zhuo.im.codec.config.BootstrapConfig;
import com.zhuo.im.tcp.receiver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;

/**
 * @description:
 * @version: 1.0
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginMode;

    public static void init(BootstrapConfig.TcpConfig tcpConfig){
        loginMode = tcpConfig.getLoginMode();

        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(tcpConfig.getRedis());

        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginMode);
        userLoginMessageListener.listenerUserLogin();
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }

}
