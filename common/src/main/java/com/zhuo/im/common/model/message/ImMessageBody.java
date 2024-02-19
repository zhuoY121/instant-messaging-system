package com.zhuo.im.common.model.message;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class ImMessageBody {
    private Integer appId;

    // messageBodyId
    private Long messageKey;

    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;
}
