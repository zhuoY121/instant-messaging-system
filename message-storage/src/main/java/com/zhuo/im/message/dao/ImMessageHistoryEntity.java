package com.zhuo.im.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 **/
@Data
@TableName("im_message_history")
public class ImMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String toId;

    private String ownerId;

    // messageBodyId
    private Long messageKey;

    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;

}
