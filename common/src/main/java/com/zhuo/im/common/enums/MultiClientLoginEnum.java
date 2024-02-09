package com.zhuo.im.common.enums;

public enum MultiClientLoginEnum {

    /**
     * Only allow one client to be online (mobile/computer/web)
     */
    ONE(1,"  MultiClientLoginEnum_ONE"),

    /**
     * Allow one device of mobile/computer + multiple web clients to be online at the same time
     */
    TWO(2,"  MultiClientLoginEnum_TWO"),

    /**
     * Allow mobile (one device) + computer (one device) + multiple web clients to be online at the same time
     */
    THREE(3,"  MultiClientLoginEnum_THREE"),

    /**
     * Allow all clients to be online at the same time
     */
    ALL(4,"  MultiClientLoginEnum_ALL");

    private int loginMode;
    private String loginDesc;

    /**
     * Cannot use the default enumType b= enumType.values()[i]; because this enumeration is encapsulated in class form
     * @param ordinal
     * @return
     */
    public static MultiClientLoginEnum getMember(int ordinal) {
        for (int i = 0; i <  MultiClientLoginEnum.values().length; i++) {
            if (MultiClientLoginEnum.values()[i].getLoginMode() == ordinal) {
                return  MultiClientLoginEnum.values()[i];
            }
        }
        return THREE;
    }

    MultiClientLoginEnum(int loginMode, String loginDesc){
        this.loginMode = loginMode;
        this.loginDesc = loginDesc;
    }

    public int getLoginMode() {
        return loginMode;
    }

    public void setLoginMode(int loginMode) {
        this.loginMode = loginMode;
    }

    public String getLoginDesc() {
        return loginDesc;
    }

    public void setLoginDesc(String loginDesc) {
        this.loginDesc = loginDesc;
    }

}
