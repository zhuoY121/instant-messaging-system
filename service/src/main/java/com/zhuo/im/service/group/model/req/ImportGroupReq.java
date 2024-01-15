package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class ImportGroupReq extends RequestBase {

    private String groupId;
    // group owner id
    private String ownerId;

    // Group type: 1 Private group (similar to WeChat); 2 Public group (similar to QQ)
    private Integer groupType;

    @NotBlank(message = "groupName cannot be empty")
    private String groupName;

    // Whether all members are muted, 0 means no muting; 1 means all members are muted.
    private Integer mute;

    //Permission to join the group: 0 everyone can join; 1 group members can pull people; 2 group administrators or groups can pull people.
    private Integer applyJoinType;

    //Group introduction
    private String introduction;

    // Group notice
    private String notification;

    // Group photo
    private String photo;

    private Integer MaxMemberCount;

    private Long createTime;

    private String extra;

}
