package com.zhuo.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.DelFlagEnum;
import com.zhuo.im.common.model.message.*;
import com.zhuo.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.zhuo.im.service.utils.SnowflakeIdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class MessageStoreService {

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

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

}
