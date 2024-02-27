package com.zhuo.im.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.pack.user.UserStatusChangeNotificationPack;
import com.zhuo.im.codec.protocol.MessageHeader;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ImConnectStatusEnum;
import com.zhuo.im.common.enums.command.UserEventCommand;
import com.zhuo.im.common.model.UserClientDto;
import com.zhuo.im.common.model.UserSession;
import com.zhuo.im.tcp.publish.MqMessageProducer;
import com.zhuo.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static List<NioSocketChannel> getAll(Integer appId, String userId) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel -> {
            if(channel.getAppId().equals(appId) && userId.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }

    public static void remove(UserClientDto dto){
        CHANNELS.remove(dto);
    }

    public static void remove(NioSocketChannel channel){
        CHANNELS.entrySet().stream().filter(entity -> entity.getValue() == channel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    public static void removeUserSession(NioSocketChannel nioSocketChannel){

        // Get the user info from the channel
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

        // Remove from Sessions
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setUserId(userId);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        SessionSocketHolder.remove(userClientDto);

        // Remove from Redis
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType + ":" + imei);

        // Send the notification to the logic layer
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);

        UserStatusChangeNotificationPack userStatusChangeNotificationPack = new UserStatusChangeNotificationPack();
        userStatusChangeNotificationPack.setAppId(appId);
        userStatusChangeNotificationPack.setUserId(userId);
        userStatusChangeNotificationPack.setStatus(ImConnectStatusEnum.OFFLINE.getCode());
        MqMessageProducer.sendMessage(userStatusChangeNotificationPack, messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        // Close the channel
        nioSocketChannel.close();
    }

    public static void userSessionOffline(NioSocketChannel nioSocketChannel){

        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setUserId(userId);
        userClientDto.setAppId(appId);
        userClientDto.setClientType(clientType);
        userClientDto.setImei(imei);
        SessionSocketHolder.remove(userClientDto);

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType.toString() + ":" + imei);

        if (StringUtils.isNotBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE.getCode());
            map.put(clientType.toString() + ":" + imei, JSONObject.toJSONString(userSession));
        }

        // Send the notification to the logic layer
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);

        UserStatusChangeNotificationPack userStatusChangeNotificationPack = new UserStatusChangeNotificationPack();
        userStatusChangeNotificationPack.setAppId(appId);
        userStatusChangeNotificationPack.setUserId(userId);
        userStatusChangeNotificationPack.setStatus(ImConnectStatusEnum.OFFLINE.getCode());
        MqMessageProducer.sendMessage(userStatusChangeNotificationPack, messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

    }

}
