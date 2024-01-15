package com.zhuo.im.service.group.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.group.model.req.*;


/**
 * @description:
 * @version: 1.0
 */
public interface ImGroupMemberService {

    public ResponseVO importGroupMember(ImportGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);



}
