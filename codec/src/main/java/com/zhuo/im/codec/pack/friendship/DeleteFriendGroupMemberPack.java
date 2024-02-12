package com.zhuo.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;

/**
 * @description:
 **/
@Data
public class DeleteFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    private Long sequence;
}
