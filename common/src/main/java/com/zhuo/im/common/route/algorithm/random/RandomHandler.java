package com.zhuo.im.common.route.algorithm.random;

import com.zhuo.im.common.enums.UserErrorCode;
import com.zhuo.im.common.exception.ApplicationException;
import com.zhuo.im.common.route.RouteHandler;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description:
 * @version: 1.0
 */
public class RandomHandler implements RouteHandler {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);
        return values.get(i);
    }
}
