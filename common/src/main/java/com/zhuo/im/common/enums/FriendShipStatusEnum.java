package com.zhuo.im.common.enums;

public enum FriendShipStatusEnum {

    /**
     * 0=not added; 1=normal; 2=deleted
     */
    FRIEND_STATUS_NOT_FRIEND(0),

    FRIEND_STATUS_NORMAL(1),

    FRIEND_STATUS_DELETE(2),

    /**
     * 0=not added; 1=normal; 2=deleted
     */
    BLACK_STATUS_NORMAL(1),

    BLACK_STATUS_BLACKED(2),
    ;

    private int code;

    FriendShipStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
