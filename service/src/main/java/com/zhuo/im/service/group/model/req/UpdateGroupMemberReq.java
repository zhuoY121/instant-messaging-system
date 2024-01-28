package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 **/
@Data
public class UpdateGroupMemberReq extends RequestBase {

    @NotBlank(message = "groupId cannot be empty")
    private String groupId;

    @NotBlank(message = "memberId cannot be empty")
    private String memberId;

    private String alias;

    private Integer role;

    private String extra;

}
