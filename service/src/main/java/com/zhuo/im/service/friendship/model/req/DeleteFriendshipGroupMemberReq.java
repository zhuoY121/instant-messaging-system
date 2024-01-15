package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @description:
 **/
@Data
public class DeleteFriendshipGroupMemberReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

    @NotBlank(message = "group name cannot be empty")
    private String groupName;

    @NotEmpty(message = "select users to delete")
    private List<String> toIds;


}
