package com.zhuo.im.codec.pack.group;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class DeleteGroupPack {

    private String groupId;

    private Long sequence;

}
