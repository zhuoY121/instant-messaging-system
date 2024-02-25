package com.zhuo.im.service.utils;

import com.zhuo.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class WriteUserSeq {


    /**
     * Redis: uid as key, friend/group/conversation as hash, sequence number as value
     */

    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId, String userId, String type, Long seq) {
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key, type, seq);
    }

}
