package com.zhuo.im.common.constant;

/**
 * @description:
 * @version: 1.0
 */
public class Constants {

    public static class RedisConstants{


        /**
         * User session
         * Format: appId + UserSessionConstants + 用户id. For example, 10000: userSession: lld
         */
        public static final String UserSessionConstants = ":userSession:";


    }


}
