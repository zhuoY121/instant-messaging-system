package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class CheckFriendshipReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

    @NotEmpty(message = "toIds cannot be empty")
    private List<String> toIds;

    @NotNull(message = "checkType cannot be empty")
    private Integer checkType;
}
