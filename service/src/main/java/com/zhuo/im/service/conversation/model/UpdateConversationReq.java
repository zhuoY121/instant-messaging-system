package com.zhuo.im.service.conversation.model;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer muted;

    private Integer isTop;

    private String fromId;


}
