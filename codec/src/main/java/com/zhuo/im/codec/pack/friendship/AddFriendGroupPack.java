package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

/**
 * @description: User creates friend group notification package
 **/
@Data
public class AddFriendGroupPack {
    public String fromId;

    private String groupName;

    private Long sequence;
}
