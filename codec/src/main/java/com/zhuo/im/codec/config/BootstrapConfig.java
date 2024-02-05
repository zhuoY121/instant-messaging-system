package com.zhuo.im.codec.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class BootstrapConfig {

    private TcpConfig tcpConfig;

    @Data
    public static class TcpConfig {

        private Integer tcpPort;

        private Integer webSocketPort;

        private Integer bossThreadSize;

        private Integer workerThreadSize;

        private RedisConfig redis;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisConfig {

        // mode: single / sentinel / cluster
        private String mode;

        private Integer database;

        private String password;

        private Integer timeout;

        private Integer poolMinIdle;

        // Connection timeout (milliseconds)
        private Integer poolConnTimeout;

        private Integer poolSize;

        // Configurations for the single mode
        private RedisSingle single;

    }

    /**
     * Redis Configurations for the single mode
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSingle {

        private String address;
    }
}
