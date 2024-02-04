package com.zhuo.im.codec.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: The message service sends the packet body to tcp, and tcp parses the changed packet body into a Message and sends it to the client.
 **/
@Data
public class MessagePack<T> implements Serializable {

    private String userId;

    private Integer appId;

    /**
     * receiver
     */
    private String toId;

    private int clientType;

    private String messageId;

    /**
     * Client device unique identifier
     */
    private String imei;

    private Integer command;

    /**
     * Business data object. If it is a chat message, it does not need to be parsed and directly transmitted transparently.
     */
    private T data;


}
