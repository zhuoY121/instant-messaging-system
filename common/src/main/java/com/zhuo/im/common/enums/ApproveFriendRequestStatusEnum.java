package com.zhuo.im.common.enums;

public enum ApproveFriendRequestStatusEnum {

    /**
     * 1=accept；2=reject。
     */
    ACCEPT(1),

    REJECT(2),
    ;

    private int code;

    ApproveFriendRequestStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
