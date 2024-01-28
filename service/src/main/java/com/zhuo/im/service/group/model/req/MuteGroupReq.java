package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description:
 **/
@Data
public class MuteGroupReq extends RequestBase {

    @NotBlank(message = "groupId cannot be empty")
    private String groupId;

    // 0=unmuted; 1=muted
    @NotNull(message = "muted cannot be empty")
    private Integer muted;

}
