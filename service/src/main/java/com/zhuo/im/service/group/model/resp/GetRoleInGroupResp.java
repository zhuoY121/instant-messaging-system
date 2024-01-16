package com.zhuo.im.service.group.model.resp;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class GetRoleInGroupResp {

    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Long speakDate;

}
