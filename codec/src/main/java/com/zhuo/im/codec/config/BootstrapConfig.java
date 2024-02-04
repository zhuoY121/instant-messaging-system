package com.zhuo.im.codec.config;

import lombok.Data;

@Data
public class BootstrapConfig {

    private TcpConfig tcpConfig;

    @Data
    public static class TcpConfig {
        private Integer tcpPort;
        private Integer webSocketPort;
        private Integer bossThreadSize;
        private Integer workerThreadSize;

    }
}
