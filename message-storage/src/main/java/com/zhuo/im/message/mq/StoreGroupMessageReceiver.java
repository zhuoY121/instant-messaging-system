package com.zhuo.im.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.message.dao.ImMessageBodyEntity;
import com.zhuo.im.message.model.DoStoreGroupMessageDto;
import com.zhuo.im.message.service.StoreMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class StoreGroupMessageReceiver {
    private static Logger logger = LoggerFactory.getLogger(StoreGroupMessageReceiver.class);

    @Autowired
    StoreMessageService storeMessageService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitmqConstants.StoreGroupMessage, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitmqConstants.StoreGroupMessage, durable = "true")
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String,Object> headers,
                              Channel channel) throws Exception {

        String msg = new String(message.getBody(),"utf-8");
        logger.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            DoStoreGroupMessageDto doStoreGroupMessageDto = jsonObject.toJavaObject(DoStoreGroupMessageDto.class);
            ImMessageBodyEntity messageBody = jsonObject.getObject("messageBody", ImMessageBodyEntity.class);
            doStoreGroupMessageDto.setImMessageBodyEntity(messageBody);
            storeMessageService.doStoreGroupMessage(doStoreGroupMessageDto);

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            logger.error("An exception occurred while processing the message: {}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR: ", e);
            logger.error("NACK_MSG:{}", msg);

            // The first false means not to reject batches, and the second false means not to return to the queue.
            channel.basicNack(deliveryTag, false, false);
        }

    }
}
