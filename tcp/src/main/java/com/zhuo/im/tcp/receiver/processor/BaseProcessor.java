package com.zhuo.im.tcp.receiver.processor;

import com.zhuo.im.codec.protocol.MessagePack;
import com.zhuo.im.common.model.UserClientDto;
import com.zhuo.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @description:
 * @version: 1.0
 */
public abstract class BaseProcessor {

    public abstract void processBefore();

    public void process(MessagePack messagePack){

        processBefore();

        UserClientDto dto = new UserClientDto();
        dto.setAppId(messagePack.getAppId());
        dto.setUserId(messagePack.getToId());
        dto.setClientType(messagePack.getClientType());
        dto.setImei(messagePack.getImei());

        NioSocketChannel channel = SessionSocketHolder.get(dto);
        if (channel != null) {
            channel.writeAndFlush(messagePack);
        }

        processAfter();
    }

    public abstract void processAfter();

}
