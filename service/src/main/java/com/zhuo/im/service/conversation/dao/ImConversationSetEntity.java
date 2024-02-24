package com.zhuo.im.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 **/
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    // conversation id. Format: conversationType + fromId + toId
    private String conversationId;

    private Integer conversationType;

    private String fromId;

    private String toId;

    private int muted;

    private int isTop;

    private Long sequence;

    private Long readSequence;

    private Integer appId;
}
