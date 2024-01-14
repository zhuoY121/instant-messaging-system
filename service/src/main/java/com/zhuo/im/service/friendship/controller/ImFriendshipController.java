package com.zhuo.im.service.friendship.controller;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.friendship.model.req.*;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/friendship")
public class ImFriendshipController {

    @Autowired
    ImFriendshipService imFriendshipService;

    @RequestMapping("/importFriendship")
    public ResponseVO importFriendship(@RequestBody @Validated ImportFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.importFriendship(req);
    }

    @RequestMapping("/addFriendship")
    public ResponseVO addFriendship(@RequestBody @Validated AddFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.addFriendship(req);
    }

    @RequestMapping("/updateFriendship")
    public ResponseVO updateFriendship(@RequestBody @Validated UpdateFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.updateFriendship(req);
    }

    @RequestMapping("/deleteFriendship")
    public ResponseVO deleteFriendship(@RequestBody @Validated DeleteFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.deleteFriendship(req);
    }

    @RequestMapping("/deleteAllFriendship")
    public ResponseVO deleteAllFriendship(@RequestBody @Validated DeleteAllFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.deleteAllFriendship(req);
    }

    @RequestMapping("/getFriendship")
    public ResponseVO getFriendship(@RequestBody @Validated GetFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.getFriendship(req);
    }

    @RequestMapping("/getAllFriendship")
    public ResponseVO getAllFriendship(@RequestBody @Validated GetAllFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.getAllFriendship(req);
    }

    @RequestMapping("/checkFriendship")
    public ResponseVO checkFriendship(@RequestBody @Validated CheckFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.checkFriendship(req);
    }

    @RequestMapping("/addBlack")
    public ResponseVO addBlack(@RequestBody @Validated AddFriendshipBlackReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.addBlack(req);
    }

    @RequestMapping("/deleteBlack")
    public ResponseVO deleteBlack(@RequestBody @Validated DeleteBlackReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.deleteBlack(req);
    }

    @RequestMapping("/checkBlack")
    public ResponseVO checkBlack(@RequestBody @Validated CheckFriendshipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendshipService.checkBlack(req);
    }
}
