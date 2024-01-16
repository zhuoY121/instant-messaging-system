package com.zhuo.im.service.group.controller;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.group.model.req.*;
import com.zhuo.im.service.group.service.ImGroupMemberService;
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
@RequestMapping("v1/group/member")
public class ImGroupMemberController {

    @Autowired
    ImGroupService groupService;

    @Autowired
    ImGroupMemberService groupMemberService;

    @RequestMapping("/importGroupMember")
    public ResponseVO importGroupMember(@RequestBody @Validated ImportGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupMemberService.importGroupMember(req);
    }

    @RequestMapping("/update")
    public ResponseVO update(@RequestBody @Validated UpdateGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.updateBaseGroupInfo(req);
    }

}
