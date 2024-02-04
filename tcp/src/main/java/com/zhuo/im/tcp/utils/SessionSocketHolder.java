package com.zhuo.im.tcp.utils;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @version: 1.0
 */
public class SessionSocketHolder {

    private static final Map<String, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(String userId, NioSocketChannel channel){
        CHANNELS.put(userId, channel);
    }

    public static NioSocketChannel get(String userId) {
        return CHANNELS.get(userId);
    }

}
