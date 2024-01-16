package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class ImportGroupMemberReq extends RequestBase {

    @NotBlank(message = "group id cannot be empty")
    private String groupId;

    private List<GroupMemberDto> memberList;

}
