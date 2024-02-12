package com.zhuo.im.codec.pack.group;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class UpdateGroupInfoPack {

    private String groupId;

    private String groupName;

    private Integer mute;

    private Integer joinType;

    private String introduction;

    private String notification;

    private String photo;

    private Integer maxMemberCount;

    private Long sequence;
}
