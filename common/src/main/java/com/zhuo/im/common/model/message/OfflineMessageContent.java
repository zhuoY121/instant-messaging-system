package com.zhuo.im.common.model.message;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class OfflineMessageContent {

    private Integer appId;

    // messageBodyId
    private Long messageKey;

    // messageBody
    private String messageBody;

    private Long messageTime;

    private String extra;

    private Integer delFlag;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private String messageRandom;

    private Integer conversationType;

    private String conversationId;

}
