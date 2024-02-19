package com.zhuo.im.codec.pack.message;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class MessageReceiveServerAckPack {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Boolean serverSend;

}
