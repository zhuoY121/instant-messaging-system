package com.zhuo.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.model.SyncReq;
import com.zhuo.im.common.model.SyncResp;
import com.zhuo.im.common.model.message.MessageReadContent;
import com.zhuo.im.common.model.message.MessageReadPack;
import com.zhuo.im.common.model.message.MessageReceiveAckContent;
import com.zhuo.im.common.model.message.OfflineMessageContent;
import com.zhuo.im.service.conversation.service.ConversationService;
import com.zhuo.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
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
}
