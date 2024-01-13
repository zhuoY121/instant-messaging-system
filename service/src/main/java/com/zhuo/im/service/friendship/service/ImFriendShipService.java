package com.zhuo.im.service.friendship.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.*;

/**
 * @description:
 * @version: 1.0
 */
public interface ImFriendShipService {

    public ResponseVO importFriendShip(ImportFriendShipReq req);

    public ResponseVO addFriend(AddFriendReq req);

    public ResponseVO updateFriend(UpdateFriendReq req);

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO deleteAllFriend(DeleteAllFriendReq req);

    public ResponseVO getFriendship(GetFriendshipReq req);

    public ResponseVO getAllFriendship(GetAllFriendshipReq req);

}
