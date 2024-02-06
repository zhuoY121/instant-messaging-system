package com.zhuo.im.tcp.server;

import com.zhuo.im.codec.MessageDecoder;
import com.zhuo.im.codec.config.BootstrapConfig;
import com.zhuo.im.tcp.handler.HeartBeatHandler;
import com.zhuo.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ImServer {

    private final static Logger logger = LoggerFactory.getLogger(ImServer.class);

    BootstrapConfig.TcpConfig tcpConfig;

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ServerBootstrap server;

    public ImServer(BootstrapConfig.TcpConfig tcpConfig) {

        this.tcpConfig = tcpConfig;

        bossGroup = new NioEventLoopGroup(tcpConfig.getBossThreadSize());
        workerGroup = new NioEventLoopGroup(tcpConfig.getWorkerThreadSize());
        server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // Server-side connectable queue size
                .option(ChannelOption.SO_REUSEADDR, true) // Parameter indicates that local address and port are allowed to be reused
                .childOption(ChannelOption.TCP_NODELAY, true) // Whether to disable the Nagle algorithm. To put it simply, whether to send data in batches. True to turn off. False to turn on. If enabled, it can reduce a certain amount of network overhead, but will affect the real-time nature of messages.
                .childOption(ChannelOption.SO_KEEPALIVE, true) // If the keep-alive switch has no data for 2 hours, the server will send a heartbeat packet.
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 1));
                        ch.pipeline().addLast(new HeartBeatHandler(tcpConfig.getHeartBeatTime()));
                        ch.pipeline().addLast(new NettyServerHandler());
                    }
                });

    }

    public void start() {
        this.server.bind(this.tcpConfig.getTcpPort());
    }

}
