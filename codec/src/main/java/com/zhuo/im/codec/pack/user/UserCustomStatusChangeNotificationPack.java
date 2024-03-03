package com.zhuo.im.codec.pack.user;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UserCustomStatusChangeNotificationPack {

    private String customText;

    private Integer customStatus;

    private String userId;

}
