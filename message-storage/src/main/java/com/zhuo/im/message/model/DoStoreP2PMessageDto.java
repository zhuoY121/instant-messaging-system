package com.zhuo.im.message.model;

import com.zhuo.im.common.model.message.MessageContent;
import com.zhuo.im.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @description:
 **/
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
