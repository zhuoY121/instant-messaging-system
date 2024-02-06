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

    public static final String ReadTime = "readTime";

    public static class RedisConstants{


        /**
         * User session
         * Format: appId + UserSessionConstants + userId.
         * For example, 10000:userSession:zhuo
         */
        public static final String UserSessionConstants = ":userSession:";


    }

    public static class RabbitConstants {

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String MessageService2Im = "messageService2Pipeline";
    }


}
