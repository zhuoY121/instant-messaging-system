package com.zhuo.im.service.config;

import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.route.RouteHandler;
import com.zhuo.im.common.route.algorithm.consistenthash.ConsistentHashHandler;
import com.zhuo.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import com.zhuo.im.common.route.algorithm.roundRobin.RoundRobinHandler;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
    public RouteHandler routerHandler() {
//        return new RandomHandler();
//        return new RoundRobinHandler();

        ConsistentHashHandler consistentHashHandler = new ConsistentHashHandler();
        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
        consistentHashHandler.setHash(treeMapConsistentHash);
        return consistentHashHandler;
    }


}
