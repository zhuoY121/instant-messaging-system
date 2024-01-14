package com.zhuo.im.service.friendship.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.zhuo.im.service.friendship.model.req.FriendshipDto;
import com.zhuo.im.service.friendship.model.req.GetFriendshipRequestReq;
import com.zhuo.im.service.friendship.model.req.ReadFriendshipRequestReq;

public interface ImFriendshipRequestService {

    public ResponseVO addFriendshipRequest(String fromId, FriendshipDto dto, Integer appId);

    public ResponseVO approveFriendRequest(ApproveFriendRequestReq req);

    public ResponseVO readFriendshipRequestReq(ReadFriendshipRequestReq req);

    public ResponseVO getFriendRequest(GetFriendshipRequestReq req);

}
