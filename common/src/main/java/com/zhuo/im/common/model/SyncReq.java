package com.zhuo.im.common.model;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class SyncReq extends RequestBase {

    // Client maximum seq
    private Long lastSequence;

    // The number of data to pull at one time
    private Integer maxLimit;

}
