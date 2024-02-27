package com.zhuo.im.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.pack.user.UserStatusChangeNotificationPack;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.command.UserEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.UserSession;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.user.model.UserStatusChangeNotificationContent;
import com.zhuo.im.service.user.service.ImUserStatusService;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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
        syncSender(userStatusChangeNotifyPack, content.getUserId(), content);

        // Synchronize to friends
        sendToTargets(userStatusChangeNotifyPack, content.getUserId(), content.getAppId());
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo){
        messageProducer.sendToUserClientsExceptOne(userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFICATION_SYNC, pack, clientInfo);
    }

    private void sendToTargets(Object pack, String userId, Integer appId) {

        // Send to active friends
        List<String> allFriendship = imFriendshipService.getAllActiveFriendship(userId, appId);
        for (String fid : allFriendship) {
            messageProducer.sendToUserClients(fid,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFICATION, pack, appId);
        }

    }


}
