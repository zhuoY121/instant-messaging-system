package com.zhuo.im.common.enums;

public enum GroupTypeEnum {

    /**
     * Group type: 1 Private group (similar to WeChat); 2 Public group (similar to QQ)
     */
    PRIVATE(1),

    PUBLIC(2),

    ;

    /**
     * Cannot use the default enumType b= enumType.values()[i]; because this enumeration is encapsulated in class form
     * @param ordinal
     * @return
     */
    public static GroupTypeEnum getEnum(Integer ordinal) {

        if (ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupTypeEnum.values().length; i++) {
            if (GroupTypeEnum.values()[i].getCode() == ordinal) {
                return GroupTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
