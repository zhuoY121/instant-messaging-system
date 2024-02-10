package com.zhuo.im.service.user.model.req;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * @description:
 **/
@Data
public class LoginReq {

    @NotNull(message = "userId cannot be empty")
    private String userId;

    @NotNull(message = "appId cannot be empty")
    private Integer appId;

    private Integer clientType;
}
