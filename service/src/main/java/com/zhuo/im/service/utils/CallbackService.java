package com.zhuo.im.service.utils;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.utils.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class CallbackService {

    private Logger logger = LoggerFactory.getLogger(CallbackService.class);

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;


    public void callback(Integer appId, String callbackCommand, String jsonBody) {
        try {
            httpRequestUtils.doPost(appConfig.getCallbackUrl(), Object.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
        } catch (Exception e) {
            logger.error("Callback {}: {}. Exception occurred: {}", callbackCommand, appId, e.getMessage());
        }
    }

    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody){
        try {
            ResponseVO responseVO = httpRequestUtils.doPost("", ResponseVO.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
            return responseVO;
        } catch (Exception e){
            logger.error("Callback before {}: {}. Exception occurred: {}",callbackCommand , appId, e.getMessage());
            return ResponseVO.errorResponse();
        }
    }

    public Map<String, Object> builderUrlParams(Integer appId, String command) {
        Map<String, Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }


}
