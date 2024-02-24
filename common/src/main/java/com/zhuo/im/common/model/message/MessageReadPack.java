package com.zhuo.im.common.model.message;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class MessageReadPack {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    // Private chat or group chat
    private Integer conversationType;
}
