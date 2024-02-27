package com.zhuo.im.codec.pack.user;

import com.zhuo.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UserStatusChangeNotificationPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}
