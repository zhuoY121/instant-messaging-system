package com.zhuo.im.tcp.server;

import com.zhuo.im.codec.WebSocketMessageDecoder;
import com.zhuo.im.codec.WebSocketMessageEncoder;
import com.zhuo.im.codec.config.BootstrapConfig;
import com.zhuo.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @version: 1.0
 */
public class ImWebSocketServer {

    private final static Logger logger = LoggerFactory.getLogger(ImWebSocketServer.class);

    BootstrapConfig.TcpConfig config;
    EventLoopGroup mainGroup;
    EventLoopGroup subGroup;
    ServerBootstrap server;

    public ImWebSocketServer(BootstrapConfig.TcpConfig config) {
        this.config = config;
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // Server-side connectable queue size
                .option(ChannelOption.SO_REUSEADDR, true) // The parameter indicates that reuse of local addresses and ports is allowed
                .childOption(ChannelOption.TCP_NODELAY, true) // Whether to disable the Nagle algorithm. Simply put, whether to send data in batches is true to turn off and false to turn on. If enabled, it can reduce a certain amount of network overhead, but will affect the real-time nature of messages.
                .childOption(ChannelOption.SO_KEEPALIVE, true) // If the keep-alive switch has no data for 2 hours, the server will send a heartbeat packet.
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // websocket is based on the http protocol, so it requires an http codec
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        // Support for writing big data streams
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        // Almost all programming in netty will use this handler
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        /**
                         * The protocol handled by the websocket server, used to specify the route for client connection access: /ws
                         * This handler will help you handle some heavy and complicated things
                         * Will help you handle handshaking actions: handshaking (close, ping, pong) ping + pong = heartbeat
                         * For websocket, they are all transmitted in frames, and different data types correspond to different frames.
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                        pipeline.addLast(new WebSocketMessageDecoder());
                        pipeline.addLast(new WebSocketMessageEncoder());
                        pipeline.addLast(new NettyServerHandler(config.getBrokerId()));
                    }
                });
    }

    public void start(){
        this.server.bind(this.config.getWebSocketPort());
    }
}
