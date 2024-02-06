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

        private Long heartBeatTime; // Heartbeat timeout in milliseconds

        private RedisConfig redis;

        private RabbitmqConfig rabbitmq;

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


    /**
     * Sentinel mode configuration of rabbitmq
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RabbitmqConfig {

        private String host;

        private Integer port;

        private String virtualHost;

        private String userName;

        private String password;
    }


}
