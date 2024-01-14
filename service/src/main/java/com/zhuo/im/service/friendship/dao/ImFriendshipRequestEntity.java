package com.zhuo.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("im_friendship_request")
public class ImFriendshipRequestEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;

    private String fromId;

    private String toId;

    //    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    // Whether it has been read. 1=Read
    private Integer readStatus;

    // friend source
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String addSource;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String addMessage;

    // Approval status: 1=Accept; 2=Reject
    private Integer approveStatus;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long createTime;

    private Long updateTime;

    private Long sequence;




}
