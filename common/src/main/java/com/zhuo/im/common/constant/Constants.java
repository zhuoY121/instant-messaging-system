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

    public static final String Imei = "imei";

    public static final String ReadTime = "readTime";

    public static final String ImCoreZkRoot = "/im-coreRoot";

    public static final String ImCoreZkRootTcp = "/tcp";

    public static final String ImCoreZkRootWeb = "/web";


    public static class RedisConstants{


        /**
         * User session
         * Format: appId + UserSessionConstants + userId.
         * For example, 10000:userSession:zhuo
         */
        public static final String UserSessionConstants = ":userSession:";

        /**
         * User login notification channel
         */
        public static final String UserLoginChannel = "signal/channel/LOGIN_USER_INNER_QUEUE";

        /**
         * userSignature, format：appId:userSignature:
         */
        public static final String userSignature = "userSignature";

        /**
         * Cache client messages to prevent duplication. Format： appId + :cacheMessage: + messageId
         */
        public static final String cacheMessage = "cacheMessage";

        public static final String OfflineMessage = "offlineMessage";

        public static final String SeqPrefix = "seq";

        /**
         * User subscription list
         * Format: appId + :subscribe: + userId
         */
        public static final String subscribe = "subscribe";

    }

    public static class RabbitmqConstants {

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String StoreP2PMessage = "storeP2PMessage";

        public static final String StoreGroupMessage = "storeGroupMessage";

        public static final String Im2FriendshipService = "pipeline2FriendshipService";

        public static final String Im2UserService = "pipeline2UserService";

    }


    public static class CallbackCommand {

        public static final String ModifyUserAfter = "user.modify.after";

        public static final String AddFriendBefore = "friend.add.before";

        public static final String AddFriendAfter = "friend.add.after";

        public static final String UpdateFriendAfter = "friend.update.after";

        public static final String DeleteFriendAfter = "friend.delete.after";

        public static final String AddBlackAfter = "black.add.after";

        public static final String DeleteBlackAfter = "black.delete.after";

        public static final String CreateGroupAfter = "group.create.after";

        public static final String UpdateGroupAfter = "group.update.after";

        public static final String DeleteGroupAfter = "group.delete.after";

        public static final String AddGroupMemberBefore = "group.member.add.before";

        public static final String AddGroupMemberAfter = "group.member.add.after";

        public static final String DeleteGroupMemberAfter = "group.member.delete.after";
    }


    public static class SeqConstants {

        public static final String Message = "messageSeq";

        public static final String GroupMessage = "groupMessageSeq";

        public static final String Friendship = "friendshipSeq";

        public static final String FriendshipRequest = "friendshipRequestSeq";

        public static final String FriendshipGroup = "friendshipGroupSeq";

        public static final String Group = "groupSeq";

        public static final String Conversation = "conversationSeq";

    }
}
