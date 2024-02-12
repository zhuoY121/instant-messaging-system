package com.zhuo.im.codec.pack.friendship;

import lombok.Data;


/**
 * @description:
 **/
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;

    private Long sequence;
}
