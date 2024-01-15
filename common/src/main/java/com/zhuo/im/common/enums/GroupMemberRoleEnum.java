package com.zhuo.im.common.enums;

public enum GroupMemberRoleEnum {

    /**
     * ordinary member
     */
    ORDINARY(0),

    /**
     * group manager
     */
    MANAGER(1),

    /**
     * group owner
     */
    OWNER(2),

    /**
     *
     */
    LEFT(3);
    ;


    private int code;

    /**
     * Cannot use the default enumType b= enumType.values()[i]; because this enumeration is encapsulated in class form
     * @param ordinal
     * @return
     */
    public static GroupMemberRoleEnum getItem(int ordinal) {
        for (int i = 0; i < GroupMemberRoleEnum.values().length; i++) {
            if (GroupMemberRoleEnum.values()[i].getCode() == ordinal) {
                return GroupMemberRoleEnum.values()[i];
            }
        }
        return null;
    }

    GroupMemberRoleEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
