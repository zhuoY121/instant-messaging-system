package com.zhuo.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.jeffreyning.mybatisplus.anno.AutoMap;
import lombok.Data;

/**
 * @description:
 **/

@Data
@TableName("im_friendship")
@AutoMap
public class ImFriendshipEntity {

    @TableField(value = "app_id")
    private Integer appId;

    @TableField(value = "from_id")
    private String fromId;

    @TableField(value = "to_id")
    private String toId;

    private String remark;

    // Status: 1=Normal; 2=Delete
    private Integer status;

    // Status: 1=normal; 2=blocked
    private Integer black;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long createTime;

    // Friend relationship serial number
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long friendSequence;

    // Blacklist relationship serial number
    private Long blackSequence;

    // friend source
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String addSource;

    private String extra;

}
