package com.zhuo.im.common.enums;

public enum UserDisabledFlagEnum {


    NORMAL(0),

    DISABLED(1),
    ;

    private int code;

    UserDisabledFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
