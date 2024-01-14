package com.zhuo.im.common.model;

import lombok.Data;

@Data
public class RequestBase {

    private Integer appId;

    // People who use the API
    private String operator;
}
