package com.zhuo.im.common.model;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UserSession {

    private Integer appId;

    private String userId;

    // mobile/web
    private Integer clientType;

    // SDK version
    private Integer version;

    // Connection status. 1=online 2=offline
    private Integer connectState;

    private Integer brokerId;

    private String brokerHost;

    private String imei;

}
