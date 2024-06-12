package com.zhuo.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ConversationTypeEnum;
import com.zhuo.im.common.enums.DelFlagEnum;
import com.zhuo.im.common.model.message.*;
import com.zhuo.im.service.conversation.service.ConversationService;
import com.zhuo.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class MessageStoreService {

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AppConfig appConfig;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent){

        // Use Message Queue to asynchronously persist group chat messages
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBody);
        rabbitTemplate.convertAndSend(Constants.RabbitmqConstants.StoreP2PMessage,"",
                JSONObject.toJSONString(dto));

        messageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    public ImMessageBody extractMessageBody(MessageContent messageContent){

        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent){

        // Use Message Queue to asynchronously persist private message messages
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitmqConstants.StoreGroupMessage, "",
                JSONObject.toJSONString(dto));

        messageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent){
        // appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(messageContent),300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz){

        // appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    /**
     * @description Utilize a zSet in Redis to store offline messages for the private chat. Employ the messageKey as the score.
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage){

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();

        // Process "fromId" queue
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // Determine whether the data in the queue exceeds the set value
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey,0,0);
        }

        offlineMessage.setConversationId(conversationService.generateConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getFromId(), offlineMessage.getToId()
        ));
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());

        // Process "toId" queue
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();
        // Determine whether the data in the queue exceeds the set value
        if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(toKey,0,0);
        }

        offlineMessage.setConversationId(conversationService.generateConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getToId(), offlineMessage.getFromId()
        ));
        operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
    }

    /**
     * @description Utilize a zSet in Redis to store offline messages for the group chat. Employ the messageKey as the score.
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage, List<String> memberIdList){

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIdList) {
            String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + memberId;
            offlineMessage.setConversationId(conversationService.generateConversationId(
                    ConversationTypeEnum.GROUP.getCode(), memberId, offlineMessage.getToId()
            ));

            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey,0,0);
            }
            operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
        }
    }

}
