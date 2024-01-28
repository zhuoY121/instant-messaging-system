package com.zhuo.im.service.group.controller;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.group.model.req.*;
import com.zhuo.im.service.group.service.ImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @version: 1.0
 */
@RestController
@RequestMapping("v1/group")
public class ImGroupController {

    @Autowired
    ImGroupService groupService;

    @RequestMapping("/importGroup")
    public ResponseVO importGroup(@RequestBody @Validated ImportGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.importGroup(req);
    }

    @RequestMapping("/createGroup")
    public ResponseVO createGroup(@RequestBody @Validated CreateGroupReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.createGroup(req);
    }

    @RequestMapping("/getGroup")
    public ResponseVO getGroup(@RequestBody @Validated GetGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return groupService.getGroup(req);
    }

    @RequestMapping("/updateGroup")
    public ResponseVO update(@RequestBody @Validated UpdateGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.updateBaseGroupInfo(req);
    }

    @RequestMapping("/getJoinedGroup")
    public ResponseVO getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.getJoinedGroup(req);
    }

    @RequestMapping("/deleteGroup")
    public ResponseVO deleteGroup(@RequestBody @Validated DeleteGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.deleteGroup(req);
    }

    @RequestMapping("/transferGroup")
    public ResponseVO transferGroup(@RequestBody @Validated TransferGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.transferGroup(req);
    }

    @RequestMapping("/muteGroup")
    public ResponseVO muteGroup(@RequestBody @Validated MuteGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.muteGroup(req);
    }

}
