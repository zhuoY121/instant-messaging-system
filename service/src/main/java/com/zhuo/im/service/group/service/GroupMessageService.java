package com.zhuo.im.service.group.service;

import com.zhuo.im.codec.pack.message.ChatMessageAck;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.message.GroupChatMessageContent;
import com.zhuo.im.common.model.message.MessageContent;
import com.zhuo.im.service.group.model.req.SendGroupMessageReq;
import com.zhuo.im.service.message.model.resp.SendMessageResp;
import com.zhuo.im.service.message.service.CheckSendMessageService;
import com.zhuo.im.service.message.service.MessageStoreService;
import com.zhuo.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @description:
 * @version: 1.0
 */
@Service
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-group-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(GroupChatMessageContent messageContent){

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();


        threadPoolExecutor.execute(() -> {
            // Save to DB
            messageStoreService.storeGroupMessage(messageContent);

            // 1. Send ACK success to yourself
            ack(messageContent, ResponseVO.successResponse());

            // 2. Send messages to your other clients who are online at the same time
            senderSync(messageContent, messageContent);

            // 3. Send messages to all clients of the other party
            sendToTarget(messageContent);
        });

    }

    public ResponseVO imServerCheckPermission(String fromId, String toId, Integer appId){
        return checkSendMessageService.checkGroupMessage(fromId, toId, appId);
    }


    private void ack(MessageContent messageContent, ResponseVO responseVO) {

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        messageProducer.sendToUserClient(messageContent.getFromId(), GroupEventCommand.GROUP_MSG_ACK, responseVO, messageContent);
    }

    private void senderSync(GroupChatMessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserClientsExceptOne(messageContent.getFromId(), GroupEventCommand.GROUP_MSG, messageContent, messageContent);
    }

    private void sendToTarget(GroupChatMessageContent messageContent){

        List<String> groupMemberIdList = imGroupMemberService.getGroupMemberIdList(messageContent.getGroupId(), messageContent.getAppId());
        for (String memberId : groupMemberIdList) {
            if (!memberId.equals(messageContent.getFromId())) {
                messageProducer.sendToUserClients(memberId, GroupEventCommand.GROUP_MSG,
                        messageContent, messageContent.getAppId());
            }
        }
    }

    public SendMessageResp send(SendGroupMessageReq req) {

        GroupChatMessageContent messageContent = new GroupChatMessageContent();
        BeanUtils.copyProperties(req, messageContent);

        // Insert data
        messageStoreService.storeGroupMessage(messageContent);

        // Send messages to your other clients who are online at the same time
        senderSync(messageContent, messageContent);

        // Send messages to all clients of the other party
        sendToTarget(messageContent);

        SendMessageResp sendMessageResp = new SendMessageResp();
        sendMessageResp.setMessageKey(messageContent.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        return sendMessageResp;

    }

}
