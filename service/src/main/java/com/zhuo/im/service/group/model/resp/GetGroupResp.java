package com.zhuo.im.service.group.model.resp;

import com.zhuo.im.service.group.model.req.GroupMemberDto;
import lombok.Data;

import java.util.List;

/**
 * @description:
 **/
@Data
public class GetGroupResp {

    private String groupId;

    private Integer appId;

    // Group owner id
    private String ownerId;

    // Group type 1 Private group (similar to WeChat) 2 Public group (similar to QQ)
    private Integer groupType;

    private String groupName;

    // Whether all members are muted, 0 means no muting; 1 means all members are muted.
    private Integer mute;

    // Options for applying to join the group include the following:
    // 0 means no one is allowed to apply to join
    // 1 indicates that approval from the group owner or administrator is required
    // 2 means allowing free joining of the group without approval
    private Integer applyJoinType;

    // Whether private chat is prohibited, 0 allows group members to initiate private chat; 1 does not allow group members to initiate private chat.
    private Integer privateChat;

    // Group introduction
    private String introduction;

    // Group notice
    private String notification;

    // Group photo
    private String photo;

    // Maximum group members
    private Integer maxMemberCount;

    //Group status: 0 normal; 1 disbanded
    private Integer status;

    private List<GroupMemberDto> memberList;

}
