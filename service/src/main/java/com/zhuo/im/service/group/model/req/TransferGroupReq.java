package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class TransferGroupReq extends RequestBase {

    @NotNull(message = "groupId cannot be empty")
    private String groupId;

    private String ownerId;

}
