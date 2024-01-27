package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 **/
@Data
public class RemoveGroupMemberReq extends RequestBase {

    @NotBlank(message = "groupId cannot be empty")
    private String groupId;

    private String memberId;

}
