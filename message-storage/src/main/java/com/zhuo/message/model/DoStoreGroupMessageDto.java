package com.zhuo.message.model;

import com.zhuo.im.common.model.message.GroupChatMessageContent;
import com.zhuo.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @description:
 **/
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
