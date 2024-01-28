package com.zhuo.im.service.group.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.group.model.req.*;
import com.zhuo.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;


/**
 * @description:
 * @version: 1.0
 */
public interface ImGroupMemberService {

    public ResponseVO importGroupMember(ImportGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    public ResponseVO addGroupMember(AddGroupMemberReq req);

    public ResponseVO<GetRoleInGroupResp> getRoleInGroup(String groupId, String memberId, Integer appId);

    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);

    public ResponseVO<Collection<String>> getJoinedGroupByMember(GetJoinedGroupReq req);

    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId);

    public ResponseVO removeGroupMember(RemoveGroupMemberReq req);

    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);

    public ResponseVO updateGroupMember(UpdateGroupMemberReq req);

    public ResponseVO muteGroupMember(MuteGroupMemberReq req);
}
