package com.zhuo.im.common.enums;

public enum GroupMuteTypeEnum {

    /**
     *
     */
    NORMAL(0),


    MUTED(1),

    ;

    /**
     * @param ordinal
     * @return
     */
    public static GroupMuteTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupMuteTypeEnum.values().length; i++) {
            if (GroupMuteTypeEnum.values()[i].getCode() == ordinal) {
                return GroupMuteTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupMuteTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
