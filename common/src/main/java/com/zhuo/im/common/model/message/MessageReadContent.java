package com.zhuo.im.common.model.message;

import com.zhuo.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class MessageReadContent extends ClientInfo {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    // Private chat or group chat
    private Integer conversationType;

}
