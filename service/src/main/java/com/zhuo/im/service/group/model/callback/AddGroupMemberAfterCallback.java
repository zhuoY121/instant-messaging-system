package com.zhuo.im.service.group.model.callback;

import com.zhuo.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class AddGroupMemberAfterCallback {

    private String groupId;

    private Integer groupType;

    private String operator;

    private List<AddMemberResp> memberRespList;
}
