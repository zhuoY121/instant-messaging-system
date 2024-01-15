package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;


@Data
public class AddFriendshipGroupReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    public String fromId;

    @NotBlank(message = "groupName cannot be empty")
    private String groupName;

    private List<String> toIds;

}
