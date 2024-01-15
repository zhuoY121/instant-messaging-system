package com.zhuo.im.common.enums;

public enum GroupStatusEnum {

    /**
     * 1 Normal; 2 Disbanded. Others to be determined such as banned...
     */
    NORMAL(1),

    DESTROY(2),

    ;

    /**
     * Cannot use the default enumType b= enumType.values()[i]; because this enumeration is encapsulated in class form
     * @param ordinal
     * @return
     */
    public static GroupStatusEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupStatusEnum.values().length; i++) {
            if (GroupStatusEnum.values()[i].getCode() == ordinal) {
                return GroupStatusEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
