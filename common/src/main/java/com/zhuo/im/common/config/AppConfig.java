package com.zhuo.im.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description:
 **/
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    private String zkAddr;

    private Integer zkConnectTimeOut;

}
