package com.zhuo.im.common.enums;

public enum ConsistentHashingMethodEnum {

    /**
     * TreeMap
     */
    TREEMAP(1,"com.zhuo.im.common.route.algorithm.consistenthash" + ".TreeMapConsistentHash"),

    /**
     * custom map
     */
    CUSTOM(2,"com.zhuo.im.common.route.algorithm.consistenthash.xxxx"), // replace "xxxx" with the custom map class

    ;


    private int code;
    private String clazz;

    /**
     * Cannot use the default enumType b= enumType.values()[i]; because this enumeration is encapsulated in class form
     * @param ordinal
     * @return
     */
    public static ConsistentHashingMethodEnum getHandler(int ordinal) {
        for (int i = 0; i < ConsistentHashingMethodEnum.values().length; i++) {
            if (ConsistentHashingMethodEnum.values()[i].getCode() == ordinal) {
                return ConsistentHashingMethodEnum.values()[i];
            }
        }
        return null;
    }

    ConsistentHashingMethodEnum(int code, String clazz){
        this.code=code;
        this.clazz=clazz;
    }

    public String getClazz() {
        return clazz;
    }

    public int getCode() {
        return code;
    }
}
