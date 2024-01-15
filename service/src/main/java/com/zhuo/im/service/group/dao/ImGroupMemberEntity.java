package com.zhuo.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
@TableName("im_group_member")
public class ImGroupMemberEntity {

    @TableId(type = IdType.AUTO)
    private Long groupMemberId;

    private Integer appId;

    private String groupId;

    //
    private String memberId;

    // Group member type, 0 ordinary members, 1 administrator, 2 group owner, 3 banned, 4 removed members
    private Integer role;

    private Long speakDate;

    // group nickname
    private String alias;

    //
    private Long joinTime;

    //
    private Long leaveTime;

    private String joinType;

    private String extra;
}
