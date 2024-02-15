package com.zhuo.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.common.BaseErrorCode;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.GatewayErrorCode;
import com.zhuo.im.common.exception.ApplicationExceptionEnum;
import com.zhuo.im.common.utils.SigAPI;
import com.zhuo.im.service.user.service.ImUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class IdentityCheck {

    private static Logger logger = LoggerFactory.getLogger(IdentityCheck.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSignature(String identifier, String appId, String userSignature){

        // Check cache
        String cacheUserSig = stringRedisTemplate.opsForValue()
                .get(appId + ":" + Constants.RedisConstants.userSignature + ":" + identifier + userSignature);

        if (!StringUtils.isBlank(cacheUserSig) && Long.parseLong(cacheUserSig) >  System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }

        // Get privateKey
        String privateKey = appConfig.getPrivateKey();

        // Call sigApi to decrypt userSig
        JSONObject jsonObject = SigAPI.decodeUserSig(userSignature);

        // Take out the decrypted appid and match it with the operator and expiration time. If it fails, an error will be prompted.
        Long expireTime = 0L;
        Long expireSec = 0L;
        Long time = 0L;
        String decoderAppId = "";
        String decoderIdentifier = "";

        try {
            decoderAppId = jsonObject.getString("TLS.appId");
            decoderIdentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.get("TLS.expire").toString();
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();
            time = Long.valueOf(expireTimeStr);
            expireSec = Long.valueOf(expireStr);
            expireTime = Long.parseLong(expireTimeStr) + expireSec;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUserSignature-error:{}",e.getMessage());
        }

        if (!decoderIdentifier.equals(identifier)) {
            return GatewayErrorCode.USER_SIGNATURE_NOT_MATCH_OPERATOR;
        }

        if (!decoderAppId.equals(appId)) {
            return GatewayErrorCode.INCORRECT_USER_SIGNATURE;
        }

        if (expireSec == 0L) {
            return GatewayErrorCode.USER_SIGNATURE_EXPIRED;
        }

        if (expireTime < System.currentTimeMillis() / 1000) {
            return GatewayErrorCode.USER_SIGNATURE_EXPIRED;
        }

        // Create sigApi based on appid and private key
        SigAPI sigAPI = new SigAPI(Long.parseLong(appId), privateKey);

        // appid + "xxx" + userId + sign
        String genSig = sigAPI.genUserSig(identifier, expireSec, time,null);
        if (genSig.equalsIgnoreCase(userSignature)) {
            // Save to redis
            String key = appId + ":" + Constants.RedisConstants.userSignature + ":" + identifier + userSignature;
            long etime = expireTime - System.currentTimeMillis() / 1000;
            stringRedisTemplate.opsForValue()
                    .set(key, expireTime.toString(), etime, TimeUnit.SECONDS);

            return BaseErrorCode.SUCCESS;
        }

        return GatewayErrorCode.INCORRECT_USER_SIGNATURE;
    }


}
