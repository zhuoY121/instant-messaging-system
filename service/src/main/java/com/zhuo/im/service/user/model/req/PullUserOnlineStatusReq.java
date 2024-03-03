package com.zhuo.im.service.user.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class PullUserOnlineStatusReq extends RequestBase {

    private List<String> userList;

}
