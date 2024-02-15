package com.zhuo.im.service.message.mq;

import com.rabbitmq.client.Channel;
import com.zhuo.im.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class ChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);


    @RabbitListener(
            bindings = @QueueBinding(
                 value = @Queue(value = Constants.RabbitmqConstants.Im2MessageService, durable = "true"),
                 exchange = @Exchange(value = Constants.RabbitmqConstants.Im2MessageService, durable = "true")
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String,Object> headers,
                              Channel channel) throws Exception {

        String msg = new String(message.getBody(),"utf-8");
        logger.info("CHAT MSG FROM QUEUE ::: {}", msg);
    }


}
