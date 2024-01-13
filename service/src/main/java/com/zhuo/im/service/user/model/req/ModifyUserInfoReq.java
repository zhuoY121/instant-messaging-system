package com.zhuo.im.service.user.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description:
 */
@Data
public class ModifyUserInfoReq extends RequestBase {

    // user id
    @NotEmpty(message = "User id cannot be empty")
    private String userId;

    // nickname
    private String nickName;

    private String location;

    private String birthDay;

    private String password;

    private String photo;

    private String userSex;

    private String selfSignature;

    // Friend verification type (Friend_AllowType). 1=requires verification
    private Integer friendAllowType;

    private String extra;


}
