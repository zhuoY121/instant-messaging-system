package com.zhuo.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.model.message.MessageContent;
import com.zhuo.im.common.model.message.MessageReadContent;
import com.zhuo.im.common.model.message.MessageReceiveAckContent;
import com.zhuo.im.common.model.message.RecallMessageContent;
import com.zhuo.im.service.message.service.MessageSyncService;
import com.zhuo.im.service.message.service.P2PMessageService;
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
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class ChatMessageReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatMessageReceiver.class);

    @Autowired
    P2PMessageService p2pMessageService;

    @Autowired
    MessageSyncService messageSyncService;

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
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");

            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                // Process message
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2pMessageService.process(messageContent);

            } else if(command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
                // Message reception confirmation
                MessageReceiveAckContent messageContent = jsonObject.toJavaObject(MessageReceiveAckContent.class);
                messageSyncService.markReceived(messageContent);

            } else if(command.equals(MessageCommand.MSG_READ.getCommand())){
                // The receiver reads the message.
                MessageReadContent messageContent = jsonObject.toJavaObject(MessageReadContent.class);
                messageSyncService.markRead(messageContent);

            } else if (Objects.equals(command, MessageCommand.MSG_RECALL.getCommand())) {
                // Recall the message
                RecallMessageContent messageContent = JSON.parseObject(msg, new TypeReference<RecallMessageContent>() {}.getType());
                messageSyncService.recallMessage(messageContent);
            }

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
