package com.zhuo.im.common.model;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UserClientDto {

    private Integer appId;

    private String userId;

    private Integer clientType;

    private String imei;
}
