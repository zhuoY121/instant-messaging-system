package com.zhuo.im.common.enums;

public enum ImConnectStatusEnum {

    /**
     * Connection status. 1=online 2=offline
     */
    ONLINE(1),

    OFFLINE(2),
    ;

    private Integer code;

    ImConnectStatusEnum(Integer code){
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }
}
