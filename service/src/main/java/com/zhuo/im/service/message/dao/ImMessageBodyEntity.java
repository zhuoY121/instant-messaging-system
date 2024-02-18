package com.zhuo.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 **/
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;

    // messageBodyId
    private Long messageKey;

    private String messageBody;

    // The reserved field for encrypting the message body
    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
