package com.zhuo.im.service.user.service;

import com.zhuo.im.service.user.model.UserStatusChangeNotificationContent;
import com.zhuo.im.service.user.model.req.SetUserCustomStatusReq;
import com.zhuo.im.service.user.model.req.SubscribeUserOnlineStatusReq;


/**
 * @description:
 * @version: 1.0
 */
public interface ImUserStatusService {

    public void processUserOnlineStatusNotification(UserStatusChangeNotificationContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomStatus(SetUserCustomStatusReq req);
}
