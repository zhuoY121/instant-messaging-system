package com.zhuo.im.common.constant;

/**
 * @description:
 * @version: 1.0
 */
public class Constants {

    // userId in channel
    public static final String UserId = "userId";

    // appId in channel
    public static final String AppId = "appId";

    public static final String ClientType = "clientType";

    public static class RedisConstants{


        /**
         * User session
         * Format: appId + UserSessionConstants + 用户id.
         * For example, 10000:userSession:zhuo
         */
        public static final String UserSessionConstants = ":userSession:";


    }


}