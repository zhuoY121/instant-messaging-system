package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

/**
 * @description:
 **/
public enum GroupErrorCode implements ApplicationExceptionEnum {

    GROUP_NOT_EXIST(40000,"Group not exists"),

    GROUP_EXIST(40001,"Group already exists"),

    GROUP_OWNER_EXIST(40002, "The group already has a group owner"),

    USER_IS_IN_GROUP(40003, "This user has joined the group"),

    USER_JOIN_GROUP_ERROR(40004, "Failed to add group members"),

    GROUP_MEMBER_IS_BEYOND(40005, "The group members have reached the upper limit"),

    MEMBER_IS_NOT_IN_GROUP(40006, "This user is not in the group"),

    NEED_OWNER_OR_MANAGER_ROLE(40007, "This operation is only allowed for group owners or group managers"),

    NEED_APP_ADMIN_ROLE(40008, "This operation is only allowed for APP administrators"),

    NEED_OWNER_ROLE(40009, "This operation only allows group owners to operate"),

    CANNOT_REMOVE_GROUP_OWNER(40010, "The group owner cannot be removed"),

    UPDATE_GROUP_BASE_INFO_ERROR(40011, "Failed to update group information"),

    GROUP_MUTED(40012,"This group is muted"),

    IMPORT_GROUP_ERROR(40013, "Failed to import group"),

    NEED_YOURSELF(40014,"This operation needs to be performed by yourself"),

    CANNOT_DISBAND_PRIVATE_GROUP(40015, "Private groups are not allowed to be disbanded"),

    PUBLIC_GROUP_MUST_HAVE_OWNER(40016, "Public groups must specify a group owner"),

    GROUP_MEMBER_MUTED(40017, "Group members are muted"),

    GROUP_IS_DISBANDED(40018,"Group has been disbanded"),

    CANNOT_SET_GROUP_OWNER(41000, "Cannot set the group owner."),
    ;

    private int code;
    private String error;

    GroupErrorCode(int code, String error){
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
