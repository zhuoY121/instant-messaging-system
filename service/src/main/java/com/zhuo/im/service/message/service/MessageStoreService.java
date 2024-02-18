package com.zhuo.im.service.message.service;

import com.zhuo.im.common.enums.DelFlagEnum;
import com.zhuo.im.common.model.message.GroupChatMessageContent;
import com.zhuo.im.common.model.message.MessageContent;
import com.zhuo.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.zhuo.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.zhuo.im.service.message.dao.ImMessageBodyEntity;
import com.zhuo.im.service.message.dao.ImMessageHistoryEntity;
import com.zhuo.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.zhuo.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.zhuo.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent){

        // messageContent is converted into messageBody
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);

        // Insert messageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);

        // Convert to MessageHistory
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);

        // Batch insert
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);

        // set MessageKey
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent){

        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
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

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(
            MessageContent messageContent, ImMessageBodyEntity imMessageBodyEntity) {

        // from
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        // to
        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent){

        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);

        // Insert into im_message_body table
        imMessageBodyMapper.insert(imMessageBodyEntity);

        // Insert into im_group_message_history table
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);

        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(
            GroupChatMessageContent messageContent, ImMessageBodyEntity messageBodyEntity) {

        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }
}
