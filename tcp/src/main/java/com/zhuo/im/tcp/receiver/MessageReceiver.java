package com.zhuo.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.zhuo.im.codec.protocol.MessagePack;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.tcp.receiver.processor.BaseProcessor;
import com.zhuo.im.tcp.receiver.processor.ProcessorFactory;
import com.zhuo.im.tcp.utils.RabbitmqFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @description:
 * @version: 1.0
 */
@Slf4j
public class MessageReceiver {

    private static String brokerId;

    private static void startMessageReceiver() {
        try {
            Channel channel = RabbitmqFactory.getChannel(Constants.RabbitmqConstants.MessageService2Im + brokerId);
            channel.queueDeclare(Constants.RabbitmqConstants.MessageService2Im  + brokerId,
                    true, false, false, null
            );
            channel.queueBind(Constants.RabbitmqConstants.MessageService2Im + brokerId, Constants.RabbitmqConstants.MessageService2Im, brokerId);

            channel.basicConsume(Constants.RabbitmqConstants.MessageService2Im + brokerId, false,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            try {
                                String msgStr = new String(body);
                                log.info(msgStr);

                                MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                                BaseProcessor messageProcessor = ProcessorFactory.getMessageProcessor(messagePack.getCommand());
                                messageProcessor.process(messagePack);

                                channel.basicAck(envelope.getDeliveryTag(), false);

                            } catch (Exception e) {
                                e.printStackTrace();
                                channel.basicNack(envelope.getDeliveryTag(), false, false);
                            }
                        }
                    }
            );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        startMessageReceiver();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReceiver.brokerId)) {
            MessageReceiver.brokerId = brokerId;
        }
        startMessageReceiver();
    }

}
