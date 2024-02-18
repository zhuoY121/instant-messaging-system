package com.zhuo.im.service.message.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 **/
@Data
public class SendMessageReq extends RequestBase {

    //客户端传的messageId
    private String messageId;

    private String fromId;

    private String toId;

    private int messageRandom;

    private long messageTime;

    private String messageBody;

    // 0 means counting is required; 1 means this message does not need to be counted, that is, the icon number in the upper right corner does not increase.
    private int badgeMode;

    private Long messageLifeTime;

    private Integer appId;

}
