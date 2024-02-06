package com.zhuo.im.tcp.utils;

import com.zhuo.im.common.model.UserClientDto;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @version: 1.0
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(UserClientDto dto, NioSocketChannel channel){
        CHANNELS.put(dto, channel);
    }

    public static NioSocketChannel get(UserClientDto dto) {
        return CHANNELS.get(dto);
    }

    public static void remove(UserClientDto dto){
        CHANNELS.remove(dto);
    }

    public static void remove(NioSocketChannel channel){
        CHANNELS.entrySet().stream().filter(entity -> entity.getValue() == channel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

}
