package com.zhuo.im.service.user.model;

import com.zhuo.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @description: status indicates whether it is online or offline
 * @version: 1.0
 */
@Data
public class UserStatusChangeNotificationContent extends ClientInfo {

    private String userId;

    // Server status: 1=online; 2=offline
    private Integer status;

}
