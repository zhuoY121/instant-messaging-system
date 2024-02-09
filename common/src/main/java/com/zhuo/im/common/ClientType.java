package com.zhuo.im.common;

/**
 * @description:
 **/
public enum ClientType {

    WEBAPI(0,"WebApi"),
    WEB(1,"Web"),
    IOS(2,"iOS"),
    ANDROID(3,"Android"),
    WINDOWS(4,"Windows"),
    MAC(5,"Mac"),
    ;

    private int code;
    private String error;

    ClientType(int code, String error){
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
