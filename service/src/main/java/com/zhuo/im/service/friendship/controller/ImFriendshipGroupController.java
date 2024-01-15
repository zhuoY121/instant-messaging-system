package com.zhuo.im.service.friendship.controller;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupMemberReq;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupReq;
import com.zhuo.im.service.friendship.model.req.DeleteFriendshipGroupMemberReq;
import com.zhuo.im.service.friendship.model.req.DeleteFriendshipGroupReq;
import com.zhuo.im.service.friendship.service.ImFriendshipGroupMemberService;
import com.zhuo.im.service.friendship.service.ImFriendshipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 **/
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendshipGroupController {

    @Autowired
    ImFriendshipGroupService imFriendshipGroupService;

    @Autowired
    ImFriendshipGroupMemberService imFriendshipGroupMemberService;


    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendshipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendshipGroupService.addGroup(req);
    }

    @RequestMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendshipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendshipGroupService.deleteGroup(req);
    }

    @RequestMapping("/member/add")
    public ResponseVO memberAdd(@RequestBody @Validated AddFriendshipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendshipGroupMemberService.addGroupMember(req);
    }

    @RequestMapping("/member/del")
    public ResponseVO memberDel(@RequestBody @Validated DeleteFriendshipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendshipGroupMemberService.deleteGroupMember(req);
    }


}
