package com.zhuo.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * @description:
 **/
@Data
public class SyncResp<T> {

    private Long maxSequence;

    // Whether all data has been pulled
    private boolean completed;

    private List<T> dataList;

}
