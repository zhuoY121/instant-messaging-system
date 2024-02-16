package com.zhuo.im.common.enums;

public enum UserMutedFlagEnum {

    NORMAL(0),

    MUTED(1),
    ;

    private int code;

    UserMutedFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
