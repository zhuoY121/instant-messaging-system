package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

public enum MessageErrorCode implements ApplicationExceptionEnum {

    SENDER_MUTED(50002,"The sender is muted"),

    SENDER_DISABLED(50003,"The sender is disabled"),

    MESSAGE_BODY_NOT_EXIST(50003,"The message body does not exist"),

    MESSAGE_RECALL_TIMEOUT(50004,"The time to recall the message has exceeded"),

    MESSAGE_RECALLED(50005,"The message has been recalled"),

    ;

    private int code;
    private String error;

    MessageErrorCode(int code, String error){
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
