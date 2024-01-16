package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 **/
@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
