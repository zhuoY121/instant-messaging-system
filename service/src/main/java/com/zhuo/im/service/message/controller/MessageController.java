package com.zhuo.im.service.message.controller;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.model.SyncReq;
import com.zhuo.im.common.model.message.CheckSendMessageReq;
import com.zhuo.im.service.group.service.GroupMessageService;
import com.zhuo.im.service.message.model.req.SendMessageReq;
import com.zhuo.im.service.message.service.MessageSyncService;
import com.zhuo.im.service.message.service.P2PMessageService;
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
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    MessageSyncService messageSyncService;


    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        if (req.getCommand().equals(GroupEventCommand.GROUP_MSG.getCommand())) {
            return groupMessageService.imServerCheckPermission(req.getFromId(), req.getToId(), req.getAppId());
        }
        return p2PMessageService.imServerCheckPermission(req.getFromId(), req.getToId(), req.getAppId());
    }

    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }

}
