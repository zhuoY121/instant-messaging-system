package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class AddGroupMemberReq extends RequestBase {

    @NotBlank(message = "groupId cannot be empty")
    private String groupId;

    @NotEmpty(message = "memberList cannot be empty")
    private List<GroupMemberDto> memberList;

}
