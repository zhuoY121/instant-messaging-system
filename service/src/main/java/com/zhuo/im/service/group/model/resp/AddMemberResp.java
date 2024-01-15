package com.zhuo.im.service.group.model.resp;

import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class AddMemberResp {

    private String memberId;

    // Adding result: 0 means success; 1 means failure; 2 means already a group member
    private Integer result;

    private String resultMessage;
}
