package com.zhuo.im.service.friendship.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.*;

/**
 * @description:
 * @version: 1.0
 */
public interface ImFriendshipService {

    public ResponseVO importFriendship(ImportFriendshipReq req);

    public ResponseVO addFriendship(AddFriendshipReq req);

    public ResponseVO updateFriendship(UpdateFriendshipReq req);

    public ResponseVO deleteFriendship(DeleteFriendshipReq req);

    public ResponseVO deleteAllFriendship(DeleteAllFriendshipReq req);

    public ResponseVO getFriendship(GetFriendshipReq req);

    public ResponseVO getAllFriendship(GetAllFriendshipReq req);

    public ResponseVO checkFriendship(CheckFriendshipReq req);

    public ResponseVO addBlack(AddFriendshipBlackReq req);

    public ResponseVO deleteBlack(DeleteBlackReq req);

    public ResponseVO checkBlack(CheckFriendshipReq req);

    public ResponseVO doAddFriendship(String fromId, FriendshipDto dto, Integer appId);
}
