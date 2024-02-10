package com.zhuo.im.service.user.controller;

import com.zhuo.im.common.ClientType;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.route.RouteHandler;
import com.zhuo.im.common.route.RouteInfo;
import com.zhuo.im.common.utils.RouteInfoParseUtil;
import com.zhuo.im.service.user.model.req.ImportUserReq;
import com.zhuo.im.service.user.model.req.LoginReq;
import com.zhuo.im.service.user.service.ImUserService;
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
        RouteInfo parse = RouteInfoParseUtil.parse(s);
        return ResponseVO.successResponse(parse);
    }


}
