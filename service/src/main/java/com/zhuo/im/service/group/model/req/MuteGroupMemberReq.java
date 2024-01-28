package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description:
 **/
@Data
public class MuteGroupMemberReq extends RequestBase {

    @NotBlank(message = "groupId cannot be empty")
    private String groupId;

    @NotBlank(message = "memberId cannot be empty")
    private String memberId;

    // The mute duration in milliseconds
    @NotNull(message = "The mute duration cannot be empty")
    private Long muteDuration;
}
