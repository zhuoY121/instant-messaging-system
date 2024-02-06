package com.zhuo.im.tcp.handler;

import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @version: 1.0
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private Long heartBeatTime;

    public HeartBeatHandler(Long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // Determine whether evt is an IdleStateEvent (used to trigger user events, including read idle/write idle/read and write idle)
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("Reader idle");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("Writer idle");
            } else if (event.state() == IdleState.ALL_IDLE) {
                Long lastReadTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();
                long now = System.currentTimeMillis();

                if (lastReadTime != null && now - lastReadTime > heartBeatTime){
                    // offline != logout.
                    // logout means user logout, while offline means user is offline, but the user is still logged in
                    SessionSocketHolder.userSessionOffline((NioSocketChannel) ctx.channel());
                }

            }
        }
    }
}
