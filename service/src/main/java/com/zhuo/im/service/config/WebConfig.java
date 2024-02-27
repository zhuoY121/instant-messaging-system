package com.zhuo.im.service.config;

import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.service.interceptor.GatewayInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description:
 * @version: 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    GatewayInterceptor gatewayInterceptor;

    @Autowired
    AppConfig appConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (appConfig.isInterceptorEnabled()) {
            registry.addInterceptor(gatewayInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/v1/user/login")
                    .excludePathPatterns("/v1/message/checkSend");
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

}
