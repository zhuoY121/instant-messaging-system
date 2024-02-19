package com.zhuo.im.common.model.message;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBody messageBody;

}
