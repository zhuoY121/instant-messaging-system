package com.zhuo.im.service.friendship.controller;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.zhuo.im.service.friendship.model.req.GetFriendshipRequestReq;
import com.zhuo.im.service.friendship.model.req.ReadFriendshipRequestReq;
import com.zhuo.im.service.friendship.service.ImFriendshipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("v1/friendshipRequest")
public class ImFriendshipRequestController {

    @Autowired
    ImFriendshipRequestService imFriendshipRequestService;

    @RequestMapping("/approveFriendRequest")
    public ResponseVO approveFriendRequest(@RequestBody @Validated ApproveFriendRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipRequestService.approveFriendRequest(req);
    }

    @RequestMapping("/readFriendshipRequest")
    public ResponseVO readFriendshipRequestReq(@RequestBody @Validated ReadFriendshipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipRequestService.readFriendshipRequestReq(req);
    }

    @RequestMapping("/getFriendRequest")
    public ResponseVO getFriendRequest(@RequestBody @Validated GetFriendshipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipRequestService.getFriendRequest(req);
    }


}
