package com.zhuo.im.service.message.service;

import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.model.message.MessageReceiveAckContent;
import com.zhuo.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;


    public void markReceived(MessageReceiveAckContent messageReceiveAckContent){
        messageProducer.sendToUserClients(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK,messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }


}
