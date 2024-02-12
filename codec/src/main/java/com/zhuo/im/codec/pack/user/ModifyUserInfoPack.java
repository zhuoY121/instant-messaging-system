package com.zhuo.im.codec.pack.user;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class ModifyUserInfoPack {

    private String userId;

    private String nickName;

    private String password;

    private String photo;

    private String userSex;

    private String selfSignature;

    // Friend verification type (Friend_AllowType).
    // requires verification: 1
    private Integer friendAllowType;

}
