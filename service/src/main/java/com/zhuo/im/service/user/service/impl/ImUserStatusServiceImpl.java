package com.zhuo.im.service.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.pack.user.UserCustomStatusChangeNotificationPack;
import com.zhuo.im.codec.pack.user.UserStatusChangeNotificationPack;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.enums.command.UserEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.UserSession;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.user.model.UserStatusChangeNotificationContent;
import com.zhuo.im.service.user.model.req.SetUserCustomStatusReq;
import com.zhuo.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.zhuo.im.service.user.service.ImUserStatusService;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.service.utils.UserSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendshipService imFriendshipService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusNotification(UserStatusChangeNotificationContent content) {

        List<UserSession> userSession = userSessionUtils.getUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotificationPack userStatusChangeNotifyPack = new UserStatusChangeNotificationPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);

        // Send to other clients of your own
        syncSender(userStatusChangeNotifyPack, content.getUserId(), UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFICATION_SYNC, content);

        // Synchronize to friends and people who have subscribed to you
        sendToTargets(userStatusChangeNotifyPack, content.getUserId(), content.getAppId());
    }

    /**
     * The data structure in Redis is "publisher: [subscriber: expire_time, ...]"
     * If A subscribes to B and C, then in Redis:
     * B: [A: expire_time, ...]
     * C: [A: expire_time, ...]
     * @param req
     */
    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {

        Long subExpireTime = 0L;
        if (req != null && req.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        for (String beSubUserId : req.getSubUserIdList()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.subscribe + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey, req.getOperator(), subExpireTime.toString());
        }
    }

    @Override
    public void setUserCustomStatus(SetUserCustomStatusReq req) {

        UserCustomStatusChangeNotificationPack pack = new UserCustomStatusChangeNotificationPack();
        pack.setCustomStatus(req.getCustomStatus());
        pack.setCustomText(req.getCustomText());
        pack.setUserId(req.getUserId());

        stringRedisTemplate.opsForValue().set(req.getAppId() + ":" + Constants.RedisConstants.userCustomStatus
                + ":" + req.getUserId(), JSONObject.toJSONString(pack));

        // Send to other online clients
        syncSender(pack, req.getUserId(), UserEventCommand.USER_CUSTOM_STATUS_CHANGE_NOTIFICATION_SYNC,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        // Synchronize to friends and people who have subscribed to you
        sendToTargets(pack, req.getUserId(), req.getAppId());
    }

    private void syncSender(Object pack, String userId, Command command, ClientInfo clientInfo){
        messageProducer.sendToUserClientsExceptOne(userId, command, pack, clientInfo);
    }

    private void sendToTargets(Object pack, String userId, Integer appId) {

        // Send to active friends
        List<String> allFriendship = imFriendshipService.getAllActiveFriendship(userId, appId);
        for (String fid : allFriendship) {
            messageProducer.sendToUserClients(fid,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFICATION, pack, appId);
        }

        // Send to people who have temporarily subscribed
        String userKey = appId + ":" + Constants.RedisConstants.subscribe + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            String subscriber = (String) key;
            long expireTime = Long.parseLong((String) Objects.requireNonNull(stringRedisTemplate.opsForHash().get(userKey, subscriber)));

            if (expireTime > 0 && expireTime > System.currentTimeMillis()) {
                messageProducer.sendToUserClients(subscriber, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFICATION, pack, appId);
            } else { // Expired
                stringRedisTemplate.opsForHash().delete(userKey, subscriber);
            }
        }
    }


}
