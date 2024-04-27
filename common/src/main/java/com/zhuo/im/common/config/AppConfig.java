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

    private String privateKey;

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

    private boolean createGroupAfterCallback;

    private boolean modifyGroupAfterCallback;

    private boolean deleteGroupAfterCallback;

    private boolean deleteGroupMemberAfterCallback;

    private boolean addGroupMemberBeforeCallback;

    private boolean addGroupMemberAfterCallback;

    private boolean sendMessageCheckFriendship;

    private boolean sendMessageCheckBlacklist;

    private Integer deleteConversationSyncMode;

    private Integer offlineMessageCount;

    private boolean interceptorEnabled;

    private boolean sendMessageBeforeCallback;

    private boolean sendMessageAfterCallback;

}
