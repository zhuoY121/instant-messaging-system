package com.zhuo.im.tcp.receiver;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.tcp.utils.RabbitmqFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @version: 1.0
 */
@Slf4j
public class MessageReceiver {


    private static void startMessageReceiver() {
        try {
            Channel channel = RabbitmqFactory.getChannel(Constants.RabbitConstants.MessageService2Im);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im,
                    true, false, false, null
            );
            channel.queueBind(Constants.RabbitConstants.MessageService2Im, Constants.RabbitConstants.MessageService2Im, "");

            channel.basicConsume(Constants.RabbitConstants.MessageService2Im, false,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                            String msgStr = new String(body);
                            log.info(msgStr);
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


}
