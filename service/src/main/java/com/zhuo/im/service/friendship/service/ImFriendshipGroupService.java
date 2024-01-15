package com.zhuo.im.service.friendship.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.dao.ImFriendshipGroupEntity;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupReq;
import com.zhuo.im.service.friendship.model.req.DeleteFriendshipGroupReq;

/**
 * @description:
 **/
public interface ImFriendshipGroupService {

    public ResponseVO addGroup(AddFriendshipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendshipGroupReq req);

    public ResponseVO<ImFriendshipGroupEntity> getGroup(String fromId, String groupName, Integer appId);


}
