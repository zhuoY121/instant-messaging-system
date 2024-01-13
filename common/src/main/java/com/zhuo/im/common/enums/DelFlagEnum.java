package com.zhuo.im.common.enums;

public enum DelFlagEnum {

    /**
     * 0 normal；1 delete.
     */
    NORMAL(0),

    DELETE(1),
    ;

    private int code;

    DelFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
