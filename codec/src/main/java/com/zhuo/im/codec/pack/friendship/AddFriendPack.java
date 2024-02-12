package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class AddFriendPack {
    private String fromId;

    private String remark;

    private String toId;

    private String addSource;

    private String addMessage;

    private Long sequence;
}
