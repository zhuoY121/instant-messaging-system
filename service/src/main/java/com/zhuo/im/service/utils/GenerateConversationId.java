package com.zhuo.im.service.utils;


public class GenerateConversationId {

    /**
     * @description Generate the same string given fromId and toId
     * @return p2p id
     */
    public static String generateP2PId(String fromId, String toId){

        int i = fromId.compareTo(toId);
        if (i < 0) {
            return toId + "|" + fromId;
        } else if (i > 0) {
            return fromId + "|" + toId;
        }

        throw new RuntimeException("");
    }

}
