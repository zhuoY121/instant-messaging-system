package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class GetFriendshipRequestReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

}
