package com.zhuo.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.common.enums.ConversationTypeEnum;
import com.zhuo.im.common.model.message.MessageReadContent;
import com.zhuo.im.service.conversation.dao.ImConversationSetEntity;
import com.zhuo.im.service.conversation.dao.mapper.ImConversationSetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @description:
 * @version: 1.0
 */
@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;


    public String generateConversationId(Integer type, String fromId, String toId){
        return type + "_" + fromId + "_" + toId;
    }

    public void  messageMarkRead(MessageReadContent messageReadContent){

        String toId = messageReadContent.getToId();
        if (messageReadContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadContent.getGroupId();
        }
        String conversationId = generateConversationId(messageReadContent.getConversationType(),
                messageReadContent.getFromId(), toId);

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadContent.getAppId());

        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadContent, imConversationSetEntity);
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetMapper.insert(imConversationSetEntity);

        } else { // Update readSequence
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetMapper.markRead(imConversationSetEntity);
        }
    }
}
