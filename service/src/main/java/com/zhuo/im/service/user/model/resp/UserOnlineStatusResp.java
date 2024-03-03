package com.zhuo.im.service.user.model.resp;

import com.zhuo.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UserOnlineStatusResp {

    private List<UserSession> session;

    private String customText;

    private Integer customStatus;

}
