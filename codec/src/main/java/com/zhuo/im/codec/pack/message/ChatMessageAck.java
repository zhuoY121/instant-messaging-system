package com.zhuo.im.codec.pack.message;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class ChatMessageAck {

    private String messageId;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }

}
