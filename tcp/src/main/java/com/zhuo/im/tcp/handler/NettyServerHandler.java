package com.zhuo.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhuo.im.codec.pack.LoginPack;
import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ImConnectStatusEnum;
import com.zhuo.im.common.enums.command.SystemCommand;
import com.zhuo.im.common.model.UserSession;
import com.zhuo.im.tcp.redis.RedisManager;
import com.zhuo.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        Integer command = msg.getMessageHeader().getCommand();

        // login command
        if (command == SystemCommand.LOGIN.getCommand()) {

            LoginPack loginPack = JSON.parseObject(
                    JSONObject.toJSONString(msg.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType()
            );
            // Add the userId into the channel context
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(loginPack.getUserId());

            // Create the user session
            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE.getCode());

            // Save to redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + "", JSONObject.toJSONString(userSession));

            // Save the user session
            SessionSocketHolder.put(loginPack.getUserId(), (NioSocketChannel) ctx.channel());
        }

    }

}
