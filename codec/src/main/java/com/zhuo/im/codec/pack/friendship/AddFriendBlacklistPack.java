package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

/**
 * @description: After the user adds the blacklist, the tcp notification packet
 **/
@Data
public class AddFriendBlacklistPack {
    private String fromId;

    private String toId;

    private Long sequence;
}
