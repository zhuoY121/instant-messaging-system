package com.zhuo.im.codec.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.codec.protocol.MessageHeader;
import io.netty.buffer.ByteBuf;

/**
 * @description: Convert ByteBuf into Message entity and convert according to private protocol.
 * Private protocol rules:
 *      4 bytes represent command. It indicates the beginning of the message
 *      4 bytes represent version
 *      4 bytes represent clientType
 *      4 bytes represent messageType
 *      4 bytes represent appId
 *      4 bytes represent imei length
 *      4 bytes indicate data length
 *      imei string
 *      data body
 * Subsequently, the decoding method is added to the data header and decoded according to different decoding methods, such as pb, json, and now json string is used
 * @version: 1.0
 */
public class ByteBufToMessageUtils {

    public static Message convert(ByteBuf in){

        int command = in.readInt();

        int version = in.readInt();

        int clientType = in.readInt();

        int messageType = in.readInt();

        int appId = in.readInt();

        int imeiLength = in.readInt();

        int bodyLen = in.readInt();

        // Reset if we don't receive enough data
        if(in.readableBytes() < imeiLength + bodyLen){
            in.resetReaderIndex();
            return null;
        }

        // Read imei data
        byte [] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        // Read body data
        byte [] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);

        // Create the message header
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCommand(command);
        messageHeader.setVersion(version);

        messageHeader.setClientType(clientType);
        messageHeader.setMessageType(messageType);
        messageHeader.setAppId(appId);
        messageHeader.setImeiLength(imeiLength);
        messageHeader.setBodyLength(bodyLen);
        messageHeader.setImei(imei);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if(messageType == 0x0){
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }

        in.markReaderIndex();
        return message;
    }

}
