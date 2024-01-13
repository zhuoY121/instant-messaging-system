package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

public enum FriendShipErrorCode implements ApplicationExceptionEnum {


    IMPORT_SIZE_BEYOND(30000,"The number of imports exceeds the upper limit"),

    ADD_FRIEND_ERROR(30001,"Failed to add friend"),

    TO_IS_YOUR_FRIEND(30002,"The other person is already your friend"),

    TO_IS_NOT_YOUR_FRIEND(30003,"The other person is not your friend"),

    FRIEND_IS_DELETED(30004,"Friend has been deleted"),

    FRIEND_IS_BLACK(30006,"Friend has been blocked"),

    TARGET_IS_BLACK_YOU(30007,"The other party blocked you"),

    RELATIONSHIP_IS_NOT_EXIST(30008,"The relationship chain record does not exist"),

    ADD_BLACK_ERROR(30009,"Failed to add blacklist"),

    FRIEND_IS_NOT_YOUR_BLACK(30010,"The friend is no longer in your blacklist"),

    NOT_APPROVED_OTHER_MAN_REQUEST(30011,"Unable to approve other people's friend requests"),

    FRIEND_REQUEST_IS_NOT_EXIST(30012,"Friend application does not exist"),

    FRIEND_SHIP_GROUP_CREATE_ERROR(30014,"Friend group creation failed"),

    FRIEND_SHIP_GROUP_IS_EXIST(30015,"Friends group already exists"),

    FRIEND_SHIP_GROUP_IS_NOT_EXIST(30016,"Friends group does not exist"),



    ;

    private int code;
    private String error;

    FriendShipErrorCode(int code, String error){
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
