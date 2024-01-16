package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 **/
@Data
public class UpdateGroupReq extends RequestBase {

    @NotBlank(message = "group Id cannot be empty")
    private String groupId;

    private String groupName;

    // Whether all members are muted, 0 means no muting; 1 means all members are muted.
    private Integer mute;

    // Permission to join the group, 0 everyone can join; 1 group members can pull people; 2 group administrators or groups can pull people.
    private Integer applyJoinType;

    // group introduction
    private String introduction;

    // group notice
    private String notification;

    // group photo
    private String photo;

    // Maximum group members
    private Integer maxMemberCount;

    private String extra;

}
