package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class DeleteFriendBlacklistPack {

    private String fromId;

    private String toId;

    private Long sequence;
}
