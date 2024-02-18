package com.zhuo.im.service.config;

import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.enums.ConsistentHashingMethodEnum;
import com.zhuo.im.common.enums.ImUrlRoutingMethodEnum;
import com.zhuo.im.common.route.RouteHandler;
import com.zhuo.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import com.zhuo.im.common.route.algorithm.consistenthash.ConsistentHashHandler;
import com.zhuo.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import com.zhuo.im.common.route.algorithm.roundRobin.RoundRobinHandler;
import com.zhuo.im.service.utils.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;


/**
 * @description:
 * @version: 1.0
 */
@Configuration
public class BeanConfig {

    @Autowired
    AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandler routerHandler() throws Exception {

        Integer imRoutingMethod = appConfig.getImRoutingMethod();
        ImUrlRoutingMethodEnum handler = ImUrlRoutingMethodEnum.getHandler(imRoutingMethod);

        String routingMethod = handler.getClazz();
        RouteHandler routeHandler = (RouteHandler) Class.forName(routingMethod).newInstance();

        if (handler == ImUrlRoutingMethodEnum.HASH) {
            Method setHash = Class.forName(routingMethod).getMethod("setHash", AbstractConsistentHash.class);

            Integer consistentHashingMethod = appConfig.getConsistentHashingMethod();
            ConsistentHashingMethodEnum hashHandler = ConsistentHashingMethodEnum.getHandler(consistentHashingMethod);

            String hashMethod = hashHandler.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash) Class.forName(hashMethod).newInstance();

            setHash.invoke(routeHandler, consistentHash);
        }

        return routeHandler;
    }

    @Bean
    public EasySqlInjector easySqlInjector () {
        return new EasySqlInjector();
    }

    @Bean
    public SnowflakeIdWorker buildSnowflakeSeq() {
        return new SnowflakeIdWorker(0);
    }

}
