package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

public enum SequenceErrorCode implements ApplicationExceptionEnum  {

    NO_GROUPS(70000, "The user has not joined any groups")

    ;

    private int code;
    private String error;

    SequenceErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

}
