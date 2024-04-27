package com.zhuo.im.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 **/
@Data
@TableName("im_group_message_history")
public class ImGroupMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String groupId;

    // messageBodyId
    private Long messageKey;

    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;

}
