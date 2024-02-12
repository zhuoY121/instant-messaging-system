package com.zhuo.im.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * @description: Notification message for adding group members to the group
 **/
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

}
