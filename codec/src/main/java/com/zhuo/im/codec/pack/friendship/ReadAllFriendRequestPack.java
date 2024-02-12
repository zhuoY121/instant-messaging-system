package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
