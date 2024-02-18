package com.zhuo.im.tcp.feign;


import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @description:
 * @version: 1.0
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);

}
