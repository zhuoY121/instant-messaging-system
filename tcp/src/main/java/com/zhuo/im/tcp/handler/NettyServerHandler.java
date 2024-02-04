package com.zhuo.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhuo.im.codec.pack.LoginPack;
import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.common.enums.command.SystemCommand;
import com.zhuo.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

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

            // Save the channel info
            SessionSocketHolder.put(loginPack.getUserId(), (NioSocketChannel) ctx.channel());
        }

    }

}
