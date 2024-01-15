package com.zhuo.im.service.friendship.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupMemberReq;
import com.zhuo.im.service.friendship.model.req.DeleteFriendshipGroupMemberReq;

/**
 * @description:
 **/
public interface ImFriendshipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendshipGroupMemberReq req);

    public ResponseVO deleteGroupMember(DeleteFriendshipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMembers(Long groupId);
}
