package com.zhuo.im.codec.pack.group;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class GroupMessagePack {

    // messageId from the client
    private String messageId;

    private String messageKey;

    private String fromId;

    private String groupId;

    private int messageRandom;

    private long messageTime;

    private long messageSequence;

    private String messageBody;

    private int badgeMode;

    private Long messageLifeTime;

    private Integer appId;

}
