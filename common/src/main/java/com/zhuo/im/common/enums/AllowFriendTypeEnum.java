package com.zhuo.im.common.enums;

public enum AllowFriendTypeEnum {

    /**
     * verification required
     */
    NEED(2),

    /**
     * No verification required
     */
    NO_NEED(1),

    ;


    private int code;

    AllowFriendTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
