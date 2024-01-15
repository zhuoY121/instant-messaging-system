package com.zhuo.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
@TableName("im_group")
public class ImGroupEntity {

    @TableId(value = "group_id")
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

    private Long sequence;

    private Long createTime;

    private Long updateTime;

    private String extra;
}
