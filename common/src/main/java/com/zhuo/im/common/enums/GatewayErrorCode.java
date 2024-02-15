package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

/**
 * @description:
 **/
public enum GatewayErrorCode implements ApplicationExceptionEnum {

    USER_SIGNATURE_NOT_EXIST(60000,"User signature does not exist"),

    APPID_NOT_EXIST(60001,"appId does not exist"),

    OPERATOR_NOT_EXIST(60002,"The operator does not exist"),

    INCORRECT_USER_SIGNATURE(60003,"User signature is incorrect"),

    USER_SIGNATURE_NOT_MATCH_OPERATOR(60005,"User signature does not match operator"),

    USER_SIGNATURE_EXPIRED(60004,"User signature has expired"),

    ;

    private int code;
    private String error;

    GatewayErrorCode(int code, String error){
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
