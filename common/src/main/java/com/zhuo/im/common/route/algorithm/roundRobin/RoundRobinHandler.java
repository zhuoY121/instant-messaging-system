package com.zhuo.im.common.route.algorithm.roundRobin;

import com.zhuo.im.common.enums.UserErrorCode;
import com.zhuo.im.common.exception.ApplicationException;
import com.zhuo.im.common.route.RouteHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description:
 * @version: 1.0
 */
public class RoundRobinHandler implements RouteHandler {

    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVICE_NOT_AVAILABLE);
        }
        Long l = index.incrementAndGet() % size;
        if (l < 0){
            l = 0L;
        }
        return values.get(l.intValue());
    }
}
