package com.zhuo.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: Withdraw message notification message
 **/
@Data
@NoArgsConstructor
public class RecallMessageNotificationPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
