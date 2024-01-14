package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * Since in this case we will mark all friend requests as read. We only need the current user's id.
 */
@Data
public class ReadFriendshipRequestReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;
}
