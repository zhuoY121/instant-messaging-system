package com.zhuo.im.common.model.message;

import com.zhuo.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @description:
 **/
@Data
public class RecallMessageContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageTime;

    private Long messageSequence;

    private Integer conversationType;

}
