package com.zhuo.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.pack.message.ChatMessageAck;
import com.zhuo.im.codec.pack.message.MessageReceiveServerAckPack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ConversationTypeEnum;
import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.message.MessageContent;
import com.zhuo.im.common.model.message.OfflineMessageContent;
import com.zhuo.im.service.message.model.req.SendMessageReq;
import com.zhuo.im.service.message.model.resp.SendMessageResp;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.utils.CallbackService;
import com.zhuo.im.service.utils.ConversationIdGenerator;
import com.zhuo.im.service.utils.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class P2PMessageService {

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-P2P-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(MessageContent messageContent){

//        logger.info("Start processing messages: {}", messageContent.getMessageId());
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();

        // Get message from cache using messageId
        MessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(
                messageContent.getAppId(), messageContent.getMessageId(), MessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() ->{
                // Use messageFromMessageIdCache instead of messageContent because messageContent may not contain "seq".

                // 1. Send ACK success to the sender
                ack(messageFromMessageIdCache, ResponseVO.successResponse());

                // 2. Send messages to sender's other clients who are online at the same time
                senderSync(messageFromMessageIdCache, messageFromMessageIdCache);

                // 3. Send messages to all clients of the other party
                List<ClientInfo> clientInfos = sendToTarget(messageFromMessageIdCache);
                if (clientInfos.isEmpty()) {
                    // If all clients of the other party are offline, then it is necessary to indicate that this ACK was sent by the server.
                    receiveAck(messageFromMessageIdCache);
                }
            });
            return;
        }

        // Before callback
        ResponseVO responseVO = ResponseVO.successResponse();
        if (appConfig.isSendMessageAfterCallback()) {
            responseVO = callbackService.beforeCallback(messageContent.getAppId(), Constants.CallbackCommand.SendMessageBefore,
                    JSONObject.toJSONString(messageContent));
        }

        // Callback Failed
        if (!responseVO.isOk()) {
            ack(messageContent, responseVO);
            return;
        }

        // Ensure the orderliness of the messages: Get the message seq to sort the messages.
        long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.Message + ":" +
                ConversationIdGenerator.generateP2PId(messageContent.getFromId(), messageContent.getToId()));
        messageContent.setMessageSequence(seq);


        threadPoolExecutor.execute(() -> {
            // Save the message to DB
            messageStoreService.storeP2PMessage(messageContent);

            // Save offline messages
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            messageStoreService.storeOfflineMessage(offlineMessageContent);

            // 1. Send ACK success to the sender
            ack(messageContent, ResponseVO.successResponse());

            // 2. Send messages to sender's other clients who are online at the same time
            senderSync(messageContent, messageContent);

            // 3. Send messages to all clients of the other party
            List<ClientInfo> clientInfos = sendToTarget(messageContent);

            // Save messageId in cache.
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(),messageContent);

            // Check the feedback from target clients.
            if (clientInfos.isEmpty()) {
                // If all clients of the other party are offline, then it is necessary to indicate that this ACK was sent by the server.
                receiveAck(messageContent);
            }

            // After Callback
            if (appConfig.isSendMessageAfterCallback()) {
                callbackService.callback(messageContent.getAppId(), Constants.CallbackCommand.SendMessageAfter,
                        JSONObject.toJSONString(messageContent));
            }

        });
    }

    public ResponseVO imServerCheckPermission(String fromId, String toId, Integer appId){

        ResponseVO responseVO = checkSendMessageService.checkSenderMutedOrDisabled(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        responseVO = checkSendMessageService.checkFriendship(fromId, toId, appId);
        return responseVO;
    }


    private void ack(MessageContent messageContent, ResponseVO responseVO) {

        logger.info("Msg ack: msgId={}, checkResult={}",messageContent.getMessageId(),responseVO.getCode());
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(), messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);
        messageProducer.sendToUserClient(messageContent.getFromId(), MessageCommand.MSG_ACK, responseVO, messageContent);
    }

    private void senderSync(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserClientsExceptOne(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, messageContent);
    }

    private List<ClientInfo> sendToTarget(MessageContent messageContent){
        return messageProducer.sendToUserClients(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
    }


    public SendMessageResp send(SendMessageReq req) {

        MessageContent messageContent = new MessageContent();
        BeanUtils.copyProperties(req, messageContent);

        // Insert data
        messageStoreService.storeP2PMessage(messageContent);

        // Send messages to your other clients who are online at the same time
        senderSync(messageContent, messageContent);

        // Send messages to all clients of the other party
        sendToTarget(messageContent);

        SendMessageResp sendMessageResp = new SendMessageResp();
        sendMessageResp.setMessageKey(messageContent.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        return sendMessageResp;
    }

    public void receiveAck(MessageContent messageContent){

        MessageReceiveServerAckPack pack = new MessageReceiveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setServerSend(true);
        ClientInfo clientInfo = new ClientInfo(messageContent.getAppId(), messageContent.getClientType(), messageContent.getImei());
        messageProducer.sendToUserClient(messageContent.getFromId(), MessageCommand.MSG_RECEIVE_ACK, pack, clientInfo);
    }
}
