package com.zhuo.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.protocol.MessagePack;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.common.model.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    RabbitTemplate rabbitmqTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constants.RabbitmqConstants.MessageService2Im;

    public boolean sendMessage(UserSession session, Object msg){

        try {
            logger.info("Send message: " + msg);
            rabbitmqTemplate.convertAndSend(queueName,session.getBrokerId() + "", msg);
            return true;
        }catch (Exception e){
            logger.error("Error:" + e.getMessage());
            return false;
        }
    }

    // Pack the data and call sendMessage()
    public boolean sendPack(String toId, Command command, Object msg, UserSession session){

        MessagePack<JSONObject> messagePack = new MessagePack<>();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        messagePack.setData(jsonObject);

        String body = JSONObject.toJSONString(messagePack);
        return sendMessage(session, body);
    }

    // Send to all clients
    public void sendToUser(String toId, Command command, Object data, Integer appId) {
        List<UserSession> userSession = userSessionUtils.getUserSession(appId, toId);
        for (UserSession session : userSession) {
            sendPack(toId, command, data, session);
        }
    }


    // Send to a specified client of a user
    public void sendToUser(String toId, Command command, Object data, ClientInfo clientInfo) {
        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId,
                clientInfo.getClientType(), clientInfo.getImei());
        sendPack(toId, command, data, userSession);
    }

    private boolean match(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

    // Send to clients except one client
    public void sendToUserExceptClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        List<UserSession> userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId);
        for (UserSession session : userSession) {
            if (!match(session, clientInfo)) {
                sendPack(toId, command, data, session);
            }
        }
    }

}
