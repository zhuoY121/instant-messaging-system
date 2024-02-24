package com.zhuo.im.common.enums;

public enum ConversationTypeEnum {

    /**
     * 0=Private chat; 1=Group chat; 2=robot
     */
    P2P(0),

    GROUP(1),

    ROBOT(2),
    ;

    private int code;

    ConversationTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
