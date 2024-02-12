package com.zhuo.im.codec.pack.group;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class DeleteGroupMemberPack {

    private String groupId;

    private String member;

}
