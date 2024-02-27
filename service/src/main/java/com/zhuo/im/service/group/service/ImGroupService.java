package com.zhuo.im.service.group.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.model.SyncReq;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.model.req.*;

/**
 * @description:
 * @version: 1.0
 */
public interface ImGroupService {

    public ResponseVO importGroup(ImportGroupReq req);

    public ResponseVO createGroup(CreateGroupReq req);

    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    public ResponseVO getGroup(GetGroupReq req);

    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO deleteGroup(DeleteGroupReq req);

    public ResponseVO transferGroup(TransferGroupReq req);

    public ResponseVO muteGroup(MuteGroupReq req);

    ResponseVO syncJoinedGroups(SyncReq req);

}
