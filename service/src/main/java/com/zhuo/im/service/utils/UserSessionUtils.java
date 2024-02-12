package com.zhuo.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ImConnectStatusEnum;
import com.zhuo.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    

    /**
     * @description Get all user sessions
     * @param appId
     * @param userId
     * @return
     */
    public List<UserSession> getUserSession(Integer appId,String userId){

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);

        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values){
            String str = (String) o;
            UserSession session = JSONObject.parseObject(str, UserSession.class);
            if(Objects.equals(session.getConnectState(), ImConnectStatusEnum.ONLINE.getCode())){
                list.add(session);
            }
        }
        return list;
    }

    /**
     *
     * @description Get the user session
     * @return
     */
    public UserSession getUserSession(Integer appId, String userId, Integer clientType, String imei){

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session = JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }


}
