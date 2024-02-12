package com.zhuo.im.codec.pack.group;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class UpdateGroupMemberPack {

    private String groupId;

    private String memberId;

    private String alias;

    private String extra;
}
