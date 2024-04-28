package com.zhuo.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.zhuo.im.codec.pack.message.RecallMessageNotificationPack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ConversationTypeEnum;
import com.zhuo.im.common.enums.DelFlagEnum;
import com.zhuo.im.common.enums.MessageErrorCode;
import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.SyncReq;
import com.zhuo.im.common.model.SyncResp;
import com.zhuo.im.common.model.message.*;
import com.zhuo.im.message.dao.ImMessageBodyEntity;
import com.zhuo.im.service.conversation.service.ConversationService;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.utils.ConversationIdGenerator;
import com.zhuo.im.service.utils.GroupMessageProducer;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.message.dao.mapper.ImMessageBodyMapper;
import com.zhuo.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    GroupMessageProducer groupMessageProducer;


    public void markReceived(MessageReceiveAckContent messageReceiveAckContent){
        messageProducer.sendToUserClients(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK,messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }

    public void markRead(MessageReadContent messageReadContent){

        // Save to DB. The current client reads the message.
        conversationService.messageMarkRead(messageReadContent);

        MessageReadPack messageReadPack = new MessageReadPack();
        BeanUtils.copyProperties(messageReadContent, messageReadPack);

        // Notify other online clients
        syncSender(messageReadPack, messageReadContent, MessageCommand.MSG_READ_NOTIFICATION);

        // Send the receipt to the sender.
        messageProducer.sendToUserClients(messageReadContent.getToId(), MessageCommand.MSG_READ_RECEIPT,
                messageReadPack, messageReadContent.getAppId());
    }

    private void syncSender(MessageReadPack pack, MessageReadContent content, Command command){
        messageProducer.sendToUserClientsExceptOne(pack.getFromId(), command, pack, content);
    }

    public void groupMarkRead(MessageReadContent messageReadContent) {

        // Save to DB. The current client reads the message.
        conversationService.messageMarkRead(messageReadContent);

        MessageReadPack messageReadPack = new MessageReadPack();
        BeanUtils.copyProperties(messageReadContent,messageReadPack);

        // Notify other online clients
        syncSender(messageReadPack, messageReadContent, GroupEventCommand.GROUP_MSG_READ_NOTIFICATION);

        // Send the read receipt to the sender when the sender is not yourself in the group chat.
        if (!messageReadContent.getFromId().equals(messageReadContent.getToId())) {
            messageProducer.sendToUserClients(messageReadPack.getToId(), GroupEventCommand.GROUP_MSG_READ_RECEIPT,
                    messageReadContent, messageReadContent.getAppId());
        }
    }

    public ResponseVO syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperator();

        // Set seq
        // Get the maximum sequence in offline messages
        long maxSeq = 0L;
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if (CollectionUtils.isNotEmpty(set)) {
            List<ZSetOperations.TypedTuple<String>> list = new ArrayList<>(set);
            DefaultTypedTuple<String> o = (DefaultTypedTuple<String>) list.get(0);
            maxSeq = Objects.requireNonNull(o.getScore()).longValue();
        }
        resp.setMaxSequence(maxSeq);

        // set DataList
        Set<ZSetOperations.TypedTuple<String>> querySet = zSetOperations.rangeByScoreWithScores(key, req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        List<OfflineMessageContent> respList = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        // set Completed
        if (!CollectionUtils.isEmpty(respList)) {
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }

    public void recallMessage(RecallMessageContent messageContent) {

        // 1. Modify the status of historical messages

        Long messageTime = messageContent.getMessageTime();
        Long now = System.currentTimeMillis();

        RecallMessageNotificationPack pack = new RecallMessageNotificationPack();
        BeanUtils.copyProperties(messageContent, pack);

        Long maxTime = 60 * 1000 * 2L; // Max duration: 2 minutes
        if (maxTime < now - messageTime){
            // Messages that exceed a certain time cannot be withdrawn
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALL_TIMEOUT), messageContent);
            return;
        }

        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        query.eq("app_id", messageContent.getAppId());
        query.eq("message_key", messageContent.getMessageKey());
        ImMessageBodyEntity body = imMessageBodyMapper.selectOne(query);

        if (body == null) {
            // Messages that do not exist cannot be withdrawn
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_BODY_NOT_EXIST), messageContent);
            return;
        }

        if (body.getDelFlag() == DelFlagEnum.DELETE.getCode()) {
            // Deleted messages cannot be withdrawn
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALLED), messageContent);
            return;
        }

        body.setDelFlag(DelFlagEnum.DELETE.getCode()); 
        imMessageBodyMapper.update(body,query);

        // 2. Modify the status of offline messages
        if (messageContent.getConversationType() == ConversationTypeEnum.P2P.getCode()) {
            // Find the queue of fromId
            String fromKey = messageContent.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + messageContent.getFromId();
            // Find the queue of toId
            String toKey = messageContent.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + messageContent.getToId();

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
            offlineMessageContent.setMessageKey(messageContent.getMessageKey());
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            offlineMessageContent.setConversationId(conversationService.generateConversationId(offlineMessageContent.getConversationType(),
                    messageContent.getFromId(), messageContent.getToId()));
            offlineMessageContent.setMessageBody(body.getMessageBody());

            long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.Message + ":" +
                    ConversationIdGenerator.generateP2PId(messageContent.getFromId(), messageContent.getToId()));
            offlineMessageContent.setMessageSequence(seq);

            long messageKey = SnowflakeIdWorker.nextId();
            redisTemplate.opsForZSet().add(fromKey, JSONObject.toJSONString(offlineMessageContent), messageKey);
            redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offlineMessageContent), messageKey);

            // Send ack to the sender
            recallAck(pack, ResponseVO.successResponse(), messageContent);

            // Send ack to sync ends
            messageProducer.sendToUserClientsExceptOne(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFICATION, pack, messageContent);

            // Sent ack to all clients of the receiver
            messageProducer.sendToUserClients(messageContent.getToId(), MessageCommand.MSG_RECALL_NOTIFICATION, pack, messageContent.getAppId());

        } else {
            List<String> groupMemberIdList = imGroupMemberService.getGroupMemberIdList(messageContent.getToId(), messageContent.getAppId());
            long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.Message + ":" +
                    ConversationIdGenerator.generateP2PId(messageContent.getFromId(), messageContent.getToId()));

            // Send ack to the sender
            recallAck(pack, ResponseVO.successResponse(), messageContent);

            // Send ack to sync ends
            messageProducer.sendToUserClientsExceptOne(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFICATION, pack, messageContent);

            // Sent ack to group members
            for (String memberId : groupMemberIdList) {
                String toKey = messageContent.getAppId() + ":" + Constants.SeqConstants.Message + ":" + memberId;
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
                BeanUtils.copyProperties(messageContent, offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
                offlineMessageContent.setConversationId(conversationService.generateConversationId(offlineMessageContent.getConversationType(),
                        messageContent.getFromId(), messageContent.getToId()));
                offlineMessageContent.setMessageBody(body.getMessageBody());
                offlineMessageContent.setMessageSequence(seq);

                redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offlineMessageContent), seq);

                groupMessageProducer.producer(messageContent.getFromId(), MessageCommand.MSG_RECALL_NOTIFICATION, pack, messageContent);
            }

        }

    }

    private void recallAck(RecallMessageNotificationPack recallPack, ResponseVO<Object> success, ClientInfo clientInfo) {
        ResponseVO<Object> wrappedResp = success;
        messageProducer.sendToUserClient(recallPack.getFromId(), MessageCommand.MSG_RECALL_ACK, wrappedResp, clientInfo);
    }
}
