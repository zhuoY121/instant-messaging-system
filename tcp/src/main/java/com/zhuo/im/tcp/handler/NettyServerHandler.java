package com.zhuo.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhuo.im.codec.pack.LoginPack;
import com.zhuo.im.codec.pack.message.ChatMessageAck;
import com.zhuo.im.codec.pack.user.LoginAckPack;
import com.zhuo.im.codec.pack.user.UserStatusChangeNotificationPack;
import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.codec.protocol.MessagePack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ImConnectStatusEnum;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.enums.command.MessageCommand;
import com.zhuo.im.common.enums.command.SystemCommand;
import com.zhuo.im.common.enums.command.UserEventCommand;
import com.zhuo.im.common.model.UserClientDto;
import com.zhuo.im.common.model.UserSession;
import com.zhuo.im.common.model.message.CheckSendMessageReq;
import com.zhuo.im.tcp.feign.FeignMessageService;
import com.zhuo.im.tcp.publish.MqMessageProducer;
import com.zhuo.im.tcp.redis.RedisManager;
import com.zhuo.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {

        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))
                .target(FeignMessageService.class, logicUrl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        Integer command = msg.getMessageHeader().getCommand();

        if (command == SystemCommand.LOGIN.getCommand()) {  // login

            LoginPack loginPack = JSON.parseObject(
                    JSONObject.toJSONString(msg.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType()
            );

            // Add the login info to the channel context
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(msg.getMessageHeader().getClientType());
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(msg.getMessageHeader().getImei());

            // Create the user session
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msg.getMessageHeader().getImei());

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Save to redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei(), JSONObject.toJSONString(userSession));

            // Create userClientDto
            UserClientDto userClientDto = new UserClientDto();
            userClientDto.setAppId(msg.getMessageHeader().getAppId());
            userClientDto.setUserId(loginPack.getUserId());
            userClientDto.setClientType(msg.getMessageHeader().getClientType());
            userClientDto.setImei(msg.getMessageHeader().getImei());

            // Save the user session
            SessionSocketHolder.put(userClientDto, (NioSocketChannel) ctx.channel());

            // Publish login topic
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(userClientDto));

            // Send the notification to the logic layer
            UserStatusChangeNotificationPack userStatusChangeNotificationPack = new UserStatusChangeNotificationPack();
            userStatusChangeNotificationPack.setAppId(msg.getMessageHeader().getAppId());
            userStatusChangeNotificationPack.setUserId(loginPack.getUserId());
            userStatusChangeNotificationPack.setStatus(ImConnectStatusEnum.ONLINE.getCode());
            MqMessageProducer.sendMessage(userStatusChangeNotificationPack, msg.getMessageHeader(), UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

            // Send the login ACK to the client
            MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
            LoginAckPack loginAckPack = new LoginAckPack();
            loginAckPack.setUserId(loginPack.getUserId());
            loginSuccess.setCommand(SystemCommand.LOGIN_ACK.getCommand());
            loginSuccess.setData(loginAckPack);
            loginSuccess.setImei(msg.getMessageHeader().getImei());
            loginSuccess.setAppId(msg.getMessageHeader().getAppId());
            ctx.channel().writeAndFlush(loginSuccess);

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());

        } else if (command == SystemCommand.PING.getCommand()) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());

        } else if(command == MessageCommand.MSG_P2P.getCommand() || command == GroupEventCommand.GROUP_MSG.getCommand()) {

            CheckSendMessageReq req = new CheckSendMessageReq();
            req.setAppId(msg.getMessageHeader().getAppId());
            req.setCommand(msg.getMessageHeader().getCommand());

            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
            String fromId = jsonObject.getString("fromId");
            String toId = "";
            if (command == MessageCommand.MSG_P2P.getCommand()) {
                toId = jsonObject.getString("toId");
            } else {
                toId = jsonObject.getString("groupId");
            }
            req.setToId(toId);
            req.setFromId(fromId);

            ResponseVO responseVO = feignMessageService.checkSendMessage(req);
            if(responseVO.isOk()){
                MqMessageProducer.sendMessage(msg, command);
            }else{
                // If the verification fails, ACK will be returned directly.
                Integer ackCommand = 0;
                if (command == MessageCommand.MSG_P2P.getCommand()) {
                    ackCommand = MessageCommand.MSG_ACK.getCommand();
                }else {
                    ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                }

                ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                responseVO.setData(chatMessageAck);

                MessagePack<ResponseVO> ack = new MessagePack<>();
                ack.setData(responseVO);
                ack.setCommand(ackCommand);
                ctx.channel().writeAndFlush(ack);
            }

        } else {
            MqMessageProducer.sendMessage(msg, command);
        }

    }

    // Indicates that the channel is inactive, that is, the user is offline
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SessionSocketHolder.userSessionOffline((NioSocketChannel) ctx.channel());
        ctx.close();
    }

}
