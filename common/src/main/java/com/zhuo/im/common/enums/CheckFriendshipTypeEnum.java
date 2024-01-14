package com.zhuo.im.common.enums;

public enum CheckFriendshipTypeEnum {

    /**
     * 1=One-way verification; 2=Two-way verification.
     */
    SINGLE(1),

    BOTH(2),
    ;

    private int type;

    CheckFriendshipTypeEnum(int type){
        this.type=type;
    }

    public int getType() {
        return type;
    }
}
