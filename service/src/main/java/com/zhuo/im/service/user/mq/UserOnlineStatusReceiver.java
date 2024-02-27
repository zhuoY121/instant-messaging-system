package com.zhuo.im.service.user.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.command.UserEventCommand;
import com.zhuo.im.service.message.mq.ChatMessageReceiver;
import com.zhuo.im.service.user.model.UserStatusChangeNotificationContent;
import com.zhuo.im.service.user.service.ImUserStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class UserOnlineStatusReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatMessageReceiver.class);

    @Autowired
    ImUserStatusService imUserStatusService;

    /**
     * Subscribe to the MQ private chat message queue
     *
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constants.RabbitmqConstants.Im2UserService, durable = "true"),
            exchange = @Exchange(value = Constants.RabbitmqConstants.Im2UserService, durable = "true")
    ), concurrency = "1")
    @RabbitHandler
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {

        long start = System.currentTimeMillis();
        Thread t = Thread.currentThread();
        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FROM QUEUE :::::" + msg);
        // deliveryTag is used to pass back to Rabbitmq to confirm that the message is processed successfully
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand())) {
                UserStatusChangeNotificationContent content =
                        JSON.parseObject(msg, new TypeReference<UserStatusChangeNotificationContent>() {}.getType());
                imUserStatusService.processUserOnlineStatusNotification(content);
            }

            channel.basicAck(deliveryTag,false);

        }catch (Exception e){
            logger.error("An exception occurred while processing the message: {}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            // The first false means not to reject batches, and the second false means not to return to the queue.
            channel.basicNack(deliveryTag, false, false);

        }finally {
            long end = System.currentTimeMillis();
            logger.debug("channel {} basic-Ack, it costs {} ms, threadName = {}, threadId = {}", channel, end - start, t.getName(), t.getId());
        }

    }
}
