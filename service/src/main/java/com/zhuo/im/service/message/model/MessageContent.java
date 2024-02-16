package com.zhuo.im.service.message.model;

import com.zhuo.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;

}
