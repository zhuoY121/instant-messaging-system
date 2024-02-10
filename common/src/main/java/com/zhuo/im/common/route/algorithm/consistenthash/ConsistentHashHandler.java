package com.zhuo.im.common.route.algorithm.consistenthash;

import com.zhuo.im.common.route.RouteHandler;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
public class ConsistentHashHandler implements RouteHandler {

    // TreeMap
    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }
}
