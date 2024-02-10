package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

public enum UserErrorCode implements ApplicationExceptionEnum {

    IMPORT_SIZE_EXCEED(20000,"The number of imports exceeds the upper limit"),
    USER_NOT_EXIST(20001,"User does not exist"),
    SERVER_GET_USER_ERROR(20002,"Service failed to obtain user"),
    MODIFY_USER_ERROR(20003,"Update user failed"),
    SERVICE_NOT_AVAILABLE(71000, "No service available"),
    ;

    private int code;
    private String error;

    UserErrorCode(int code, String error){
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
