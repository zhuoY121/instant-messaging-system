package com.zhuo.im.common;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;


public enum BaseErrorCode implements ApplicationExceptionEnum {

    SUCCESS(200,"success"),
    SYSTEM_ERROR(90000,"Internal server error, please contact the administrator"),
    PARAMETER_ERROR(90001,"Parameter verification error"),


    ;

    private int code;
    private String error;

    BaseErrorCode(int code, String error){
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
