package com.zhuo.im.service.user.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: Database user data entity class
 **/

@Data
@TableName("im_user_data")
public class ImUserDataEntity {

    private Integer appId;

    private String userId;

    private String nickName;

    private String location;

    private String birthDay;

    private String password;

    private String photo;

    private Integer userSex;

    private String selfSignature;

    // Friend verification type (Friend_AllowType).
    // requires verification: 1
    private Integer friendAllowType;

    // Administrator prohibits users from adding friends:
    // 0 not disabled 1 disabled
    private Integer disableAddFriend;

    // Disabled flag (0 not disabled 1 disabled)
    private Integer forbiddenFlag;

    // Ban sign
    private Integer silentFlag;
    /**
     * User type
     * 1: Ordinary user; 2: Customer service; 3: Robot
     */
    private Integer userType;

    private Integer delFlag;

    private String extra;

}
