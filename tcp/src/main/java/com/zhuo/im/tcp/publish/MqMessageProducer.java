package com.zhuo.im.tcp.publish;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.common.constant.Constants;
import com.rabbitmq.client.Channel;
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
        String channelName = Constants.RabbitmqConstants.Im2MessageService;

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


}
