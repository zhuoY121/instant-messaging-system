package com.zhuo.im.tcp.handler;

import com.zhuo.im.codec.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
        System.out.println("NettyServerHandler.channelRead0 " + msg);
    }

}
