package com.zhuo.im.service.user.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
