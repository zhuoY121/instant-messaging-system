package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private String groupName;

    private Long sequence;
}
