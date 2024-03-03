package com.zhuo.im.service.user.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class SetUserCustomStatusReq extends RequestBase {

    private String userId;

    private String customText;

    private Integer customStatus;

}
