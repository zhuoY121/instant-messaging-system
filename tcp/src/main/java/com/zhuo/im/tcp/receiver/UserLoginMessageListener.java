package com.zhuo.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.protocol.MessagePack;
import com.zhuo.im.common.ClientType;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.MultiClientLoginEnum;
import com.zhuo.im.common.enums.command.SystemCommand;
import com.zhuo.im.common.model.UserClientDto;
import com.zhuo.im.tcp.redis.RedisManager;
import com.zhuo.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
public class UserLoginMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginMode;

    public UserLoginMessageListener(Integer loginMode) {
        this.loginMode = loginMode;
    }

    public void listenerUserLogin(){

        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                logger.info("Receive user login notification: " + msg);
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);

                // Get all online clients of the user in the current netty server.
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.getAll(dto.getAppId(), dto.getUserId());

                // Take old clients offline based on the loginMode
                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {

                    if (loginMode == MultiClientLoginEnum.ONE.getLoginMode()) {
                        // Get clientType and in the current channel
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        // The client that wants to log in is not in the channel, then we need to Log out the old client
                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            sendMultiClientLoginPack(nioSocketChannel);
                        }

                    } else if ( loginMode == MultiClientLoginEnum.TWO.getLoginMode()){
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        // If old client is a web client OR new client is a web client, then do nothing.
                        if (dto.getClientType() == ClientType.WEB.getCode() || clientType == ClientType.WEB.getCode()) {
                            continue;
                        }

                        // Log out the old client
                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            sendMultiClientLoginPack(nioSocketChannel);
                        }

                    } else if ( loginMode ==  MultiClientLoginEnum.THREE.getLoginMode()) {
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        boolean isSameClient = false;

                        // Check whether old client and new client are both mobile clients
                        if ((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode()) &&
                                (dto.getClientType() == ClientType.IOS.getCode() || dto.getClientType() == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }

                        // Check whether old client and new client are both PC clients
                        if ((clientType == ClientType.MAC.getCode() || clientType == ClientType.WINDOWS.getCode()) &&
                                (dto.getClientType() == ClientType.MAC.getCode() || dto.getClientType() == ClientType.WINDOWS.getCode())){
                            isSameClient = true;
                        }

                        if (isSameClient && !(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            sendMultiClientLoginPack(nioSocketChannel);
                        }

                    } else {
                        // Allow all clients to be online at the same time, so do nothing
                    }
                }


            }
        });
    }

    private void sendMultiClientLoginPack(NioSocketChannel nioSocketChannel) {
        MessagePack<Object> pack = new MessagePack<>();
        pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
        pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
        pack.setCommand(SystemCommand.MULTI_CLIENT_LOGIN.getCommand());
        nioSocketChannel.writeAndFlush(pack);
    }

}
