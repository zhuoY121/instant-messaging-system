package com.zhuo.im.service.group.model.req;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */

@Data
public class GroupMemberDto {

    private String memberId;

    private String alias;

    // Group member type: 0 ordinary member, 1 administrator, 2 group owner, 3 removed members.
    // When modifying group member information, it can only take the value 0/1.
    // Other values are implemented by other interfaces and are not supported yet. 3
    private Integer role;

    //    private Integer speakFlag;

    private Long speakDate;

    private String joinType;

    private Long joinTime;

}
