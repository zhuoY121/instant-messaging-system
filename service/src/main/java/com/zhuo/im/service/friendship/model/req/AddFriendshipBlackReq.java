package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class AddFriendshipBlackReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

    @NotBlank(message = "toId cannot be empty")
    private String toId;

}
