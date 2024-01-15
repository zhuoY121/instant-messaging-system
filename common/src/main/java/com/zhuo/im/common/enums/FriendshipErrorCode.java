package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

public enum FriendshipErrorCode implements ApplicationExceptionEnum {


    IMPORT_SIZE_BEYOND(30000,"The number of imports exceeds the upper limit"),

    ADD_FRIEND_ERROR(30001,"Failed to add friend"),

    TO_IS_YOUR_FRIEND(30002,"The other person is already your friend"),

    TO_IS_NOT_YOUR_FRIEND(30003,"The other person is not your friend"),

    FRIEND_IS_DELETED(30004,"Friend has been deleted"),

    FRIEND_IS_BLACK(30006,"Friend has been blocked"),

    TARGET_IS_BLACK_YOU(30007,"The other party blocked you"),

    RELATIONSHIP_IS_NOT_EXIST(30008,"The relationship chain record does not exist"),

    ADD_BLACK_ERROR(30009,"Failed to add blacklist"),

    FRIEND_IS_NOT_BLACK(30010,"The friend is not in your blacklist"),

    FRIEND_REQUEST_RECIPIENT_NOT_MATCHED(30011,"Friend request recipient does not match"),

    FRIEND_REQUEST_NOT_EXIST(30012,"Friend application does not exist"),

    FRIENDSHIP_GROUP_CREATE_ERROR(30014,"Friend group creation failed"),

    FRIENDSHIP_GROUP_EXIST(30015,"Friends group already exists"),

    FRIENDSHIP_GROUP_NOT_EXIST(30016,"Friends group does not exist"),

    FRIENDSHIP_GROUP_UPDATE_ERROR(30017,"Failed to update the group."),

    ;

    private int code;
    private String error;

    FriendshipErrorCode(int code, String error){
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
