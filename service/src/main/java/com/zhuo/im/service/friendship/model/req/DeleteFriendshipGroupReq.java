package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @description: Delete a group and delete members under the group
 **/
@Data
public class DeleteFriendshipGroupReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

    @NotEmpty(message = "groupName cannot be empty")
    private List<String> groupNames;

}
