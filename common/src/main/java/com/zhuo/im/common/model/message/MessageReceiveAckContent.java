package com.zhuo.im.common.model.message;

import com.zhuo.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class MessageReceiveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

}
