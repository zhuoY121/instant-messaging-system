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

    public static void sendMessage(Message message){

        Channel channel = null;
        String channelName = "";

        try {
            channel = RabbitmqFactory.getChannel(channelName);
            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            channel.basicPublish(channelName,"", null, o.toJSONString().getBytes());

        }catch (Exception e){
            log.error("Exception occurred when sending message: {}", e.getMessage());
        }
    }


}
