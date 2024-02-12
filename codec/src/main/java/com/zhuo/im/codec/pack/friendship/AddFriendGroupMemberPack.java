package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;

/**
 * @description:
 **/
@Data
public class AddFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    private Long sequence;
}
