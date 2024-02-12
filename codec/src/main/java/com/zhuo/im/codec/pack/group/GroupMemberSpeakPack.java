package com.zhuo.im.codec.pack.group;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class GroupMemberSpeakPack {

    private String groupId;

    private String memberId;

    private Long speakDate;

}
