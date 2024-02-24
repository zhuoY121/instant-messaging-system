package com.zhuo.im.common.enums;

import com.zhuo.im.common.exception.ApplicationExceptionEnum;

/**
 * @description:
 **/
public enum ConversationErrorCode implements ApplicationExceptionEnum {

    UPDATE_CONVERSATION_PARAMETER_ERROR(50000,"Wrong parameters for update conversation"),


    ;

    private int code;
    private String error;

    ConversationErrorCode(int code, String error){
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
