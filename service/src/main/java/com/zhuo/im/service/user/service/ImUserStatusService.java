package com.zhuo.im.service.user.service;

import com.zhuo.im.service.user.model.UserStatusChangeNotificationContent;


/**
 * @description:
 * @version: 1.0
 */
public interface ImUserStatusService {

    public void processUserOnlineStatusNotification(UserStatusChangeNotificationContent content);

}
