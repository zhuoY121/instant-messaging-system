package com.zhuo.im.service.user.service;

import com.zhuo.im.service.user.model.UserStatusChangeNotificationContent;
import com.zhuo.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.zhuo.im.service.user.model.req.PullUserOnlineStatusReq;
import com.zhuo.im.service.user.model.req.SetUserCustomStatusReq;
import com.zhuo.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.zhuo.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;


/**
 * @description:
 * @version: 1.0
 */
public interface ImUserStatusService {

    public void processUserOnlineStatusNotification(UserStatusChangeNotificationContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomStatus(SetUserCustomStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendsOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp> queryUsersOnlineStatus(PullUserOnlineStatusReq req);
}
