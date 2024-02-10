package com.zhuo.im.common.enums;

public enum ImUrlRoutingMethodEnum {


    RANDOM(1,"com.zhuo.im.common.route.algorithm.random.RandomHandler"),

    ROUND_ROBIN(2,"com.zhuo.im.common.route.algorithm.roundRobin.RoundRobinHandler"),

    HASH(3,"com.zhuo.im.common.route.algorithm.consistenthash.ConsistentHashHandler"),
    ;


    private int code;
    private String clazz;

    /**
     * Cannot use the default enumType b= enumType.values()[i]; because this enumeration is encapsulated in class form
     * @param ordinal
     * @return
     */
    public static ImUrlRoutingMethodEnum getHandler(int ordinal) {
        for (int i = 0; i < ImUrlRoutingMethodEnum.values().length; i++) {
            if (ImUrlRoutingMethodEnum.values()[i].getCode() == ordinal) {
                return ImUrlRoutingMethodEnum.values()[i];
            }
        }
        return null;
    }

    ImUrlRoutingMethodEnum(int code, String clazz){
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
