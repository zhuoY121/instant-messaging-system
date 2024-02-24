package com.zhuo.im.service.conversation.model;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class DeleteConversationReq extends RequestBase {

    @NotBlank(message = "conversationId cannot be empty")
    private String conversationId;

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

}
