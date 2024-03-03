package com.zhuo.im.service.user.controller;

import com.zhuo.im.common.ClientType;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.route.RouteHandler;
import com.zhuo.im.common.route.RouteInfo;
import com.zhuo.im.common.utils.RouteInfoParseUtils;
import com.zhuo.im.service.user.model.req.*;
import com.zhuo.im.service.user.service.ImUserService;
import com.zhuo.im.service.user.service.ImUserStatusService;
import com.zhuo.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @Autowired
    RouteHandler routeHandler;

    @Autowired
    ZKit zKit;

    @Autowired
    ImUserStatusService imUserStatusService;

    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.importUser(req);
    }

    /**
     * @description The login interface of the IM-system, it will return the address of the IM-system.
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {

        req.setAppId(appId);

        ResponseVO login = imUserService.login(req);
        if (!login.isOk()) {
            return ResponseVO.errorResponse();
        }

        List<String> nodes = new ArrayList<>();
        if (req.getClientType() == ClientType.WEB.getCode()) {
            nodes = zKit.getAllWebNodes();
        } else {
            nodes = zKit.getAllTcpNodes();
        }

        // ip:port
        String s = routeHandler.routeServer(nodes, req.getUserId());
        RouteInfo parse = RouteInfoParseUtils.parse(s);
        return ResponseVO.successResponse(parse);
    }

    @RequestMapping("/getUserSequences")
    public ResponseVO getUserSequences(@RequestBody @Validated GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequences(req);
    }

    @RequestMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated SubscribeUserOnlineStatusReq req,
                                                Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/setUserStatus")
    public ResponseVO setUserCustomStatus(@RequestBody @Validated SetUserCustomStatusReq req,
                                            Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        imUserStatusService.setUserCustomStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/queryFriendsOnlineStatus")
    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated PullFriendOnlineStatusReq req,
                                              Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryFriendsOnlineStatus(req));
    }

    @RequestMapping("/queryUsersOnlineStatus")
    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated PullUserOnlineStatusReq req,
                                            Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperator(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryUsersOnlineStatus(req));
    }

}
