package com.zhuo.im.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description:
 **/
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    private String zkAddr;

    private Integer zkConnectTimeOut;

    private Integer imRoutingMethod;

    private Integer consistentHashingMethod;

    private String callbackUrl;

    private boolean modifyUserAfterCallback;

    private boolean addFriendAfterCallback;

    private boolean addFriendBeforeCallback;

    private boolean modifyFriendAfterCallback;

    private boolean deleteFriendAfterCallback;

    private boolean addFriendShipBlackAfterCallback;

    private boolean deleteFriendShipBlackAfterCallback;

}
