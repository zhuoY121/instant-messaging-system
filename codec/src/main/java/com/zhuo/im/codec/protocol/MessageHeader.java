package com.zhuo.im.codec.protocol;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class MessageHeader {

    // 4 bytes. message operation instructions. hexadecimal. The beginning of a message usually starts with 0x
    private Integer command;
    // 4 bytes
    private Integer version;
    // 4 bytes
    private Integer clientType;

    /**
     * The data parsing type. It has nothing to do with the specific business.
     * Subsequent data parsing is based on the parsing type. 0x0: Json, 0x1: ProtoBuf, 0x2: Xml, default: 0x0
     */
    //4 bytes
    private Integer messageType = 0x0;

    // 4 bytes
    private Integer appId;

    // 4 bytes
    private Integer imeiLength;

    //4 bytes. Body length
    private int bodyLength;

    // imei string
    private String imei;
}
