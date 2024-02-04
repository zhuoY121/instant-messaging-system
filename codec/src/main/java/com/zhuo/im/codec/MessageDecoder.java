package com.zhuo.im.codec;

import com.zhuo.im.codec.protocol.Message;
import com.zhuo.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

        // Request header (
            //  command
            //  version
            //  clientType
            //  message_parsing_type
            //  imei_length
            //  appId
            //  body_len) + imei number + request body
        //
        // total len = 4*7 + imei + body

        if (in.readableBytes() < 28) {
            return;
        }

        Message message = ByteBufToMessageUtils.convert(in);
        if (message == null) {
            return;
        }

        out.add(message);

    }


}
