package com.zhuo.im.service.message.service;

import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.model.message.MessageReadContent;
import com.zhuo.im.common.model.message.MessageReadPack;
import com.zhuo.im.common.model.message.MessageReceiveAckContent;
import com.zhuo.im.service.conversation.service.ConversationService;
import com.zhuo.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    ConversationService conversationService;

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

}
