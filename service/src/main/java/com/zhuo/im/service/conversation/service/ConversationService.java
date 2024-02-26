package com.zhuo.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.codec.pack.conversation.DeleteConversationPack;
import com.zhuo.im.codec.pack.conversation.UpdateConversationPack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ConversationErrorCode;
import com.zhuo.im.common.enums.ConversationTypeEnum;
import com.zhuo.im.common.enums.command.ConversationEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.message.MessageReadContent;
import com.zhuo.im.service.conversation.dao.ImConversationSetEntity;
import com.zhuo.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.zhuo.im.service.conversation.model.DeleteConversationReq;
import com.zhuo.im.service.conversation.model.UpdateConversationReq;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.service.utils.WriteUserSeq;
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

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;


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

        long seq = redisSeq.doGetSeq(messageReadContent.getAppId() + ":" + Constants.SeqConstants.Conversation);

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadContent.getAppId());

        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadContent, imConversationSetEntity);
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);

        } else { // Update readSequence
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.markRead(imConversationSetEntity);
        }

        writeUserSeq.writeUserSeq(messageReadContent.getAppId(), messageReadContent.getFromId(), Constants.SeqConstants.Conversation, seq);
    }

    public ResponseVO deleteConversation(DeleteConversationReq req){

        // After deleting the conversation, set the conversation top status and message do not disturb status as default values.
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity != null) {
            imConversationSetEntity.setMuted(0);
            imConversationSetEntity.setIsTop(0);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
        }

        // If needed, notify other clients
        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserClientsExceptOne(req.getFromId(), ConversationEventCommand.DELETE_CONVERSATION,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO updateConversation(UpdateConversationReq req){

        if (req.getIsTop() == null && req.getMuted() == null) {
            return ResponseVO.errorResponse(ConversationErrorCode.UPDATE_CONVERSATION_PARAMETER_ERROR);
        }

        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity != null) {

            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);

            if (req.getIsTop() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getMuted() != null) {
                imConversationSetEntity.setMuted(req.getMuted());
            }
            imConversationSetEntity.setSequence(seq);

            // Update DB
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);

            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Conversation, seq);

            // Notify other clients
            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setMuted(imConversationSetEntity.getMuted());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setConversationType(imConversationSetEntity.getConversationType());
            pack.setSequence(seq);
            messageProducer.sendToUserClientsExceptOne(req.getFromId(), ConversationEventCommand.UPDATE_CONVERSATION,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

}
