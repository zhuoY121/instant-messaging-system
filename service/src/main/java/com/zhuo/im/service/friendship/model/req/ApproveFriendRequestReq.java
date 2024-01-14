package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;


@Data
public class ApproveFriendRequestReq extends RequestBase {

    private Long id;

    //1=accept; 2=reject
    private Integer status;
}
