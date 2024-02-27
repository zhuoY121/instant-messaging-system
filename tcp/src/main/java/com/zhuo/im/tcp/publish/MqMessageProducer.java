package com.zhuo.im.tcp.publish;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.codec.protocol.MessageHeader;
import com.zhuo.im.common.constant.Constants;
import com.rabbitmq.client.Channel;
import com.zhuo.im.common.enums.command.CommandType;
import com.zhuo.im.tcp.utils.RabbitmqFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @version: 1.0
 */
@Slf4j
public class MqMessageProducer {

    public static void sendMessage(Message message, Integer command){

        Channel channel = null;
        String com = command.toString();
        String commandSub = com.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);

        String channelName = "";
        if (commandType == CommandType.MESSAGE) {
            channelName = Constants.RabbitmqConstants.Im2MessageService;
        } else if (commandType == CommandType.GROUP) {
            channelName = Constants.RabbitmqConstants.Im2GroupService;
        }

        try {
            channel = RabbitmqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command", command);
            o.put("clientType", message.getMessageHeader().getClientType());
            o.put("imei", message.getMessageHeader().getImei());
            o.put("appId", message.getMessageHeader().getAppId());

            channel.basicPublish(channelName,"", null, o.toJSONString().getBytes());

        }catch (Exception e){
            log.error("Exception occurred when sending message: {}", e.getMessage());
        }
    }

    public static void sendMessage(Object message, MessageHeader header, Integer command){

        String cmd = command.toString();
        String commandSub = cmd.substring(0, 1);
        CommandType commandType = CommandType.getCommandType(commandSub);

        String channelName = "";
        if (commandType == CommandType.MESSAGE) {
            channelName = Constants.RabbitmqConstants.Im2MessageService;
        } else if(commandType == CommandType.GROUP) {
            channelName = Constants.RabbitmqConstants.Im2GroupService;
        } else if(commandType == CommandType.FRIEND) {
            channelName = Constants.RabbitmqConstants.Im2FriendshipService;
        } else if(commandType == CommandType.USER) {
            channelName = Constants.RabbitmqConstants.Im2UserService;
        }

        try {
            Channel channel = RabbitmqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message);
            o.put("command",command);
            o.put("clientType",header.getClientType());
            o.put("imei",header.getImei());
            o.put("appId",header.getAppId());

            channel.basicPublish(channelName,"", null, o.toJSONString().getBytes());

        }catch (Exception e){
            log.error("Exception occurred when sending message: {}",e.getMessage());
        }
    }

}
