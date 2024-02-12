package com.zhuo.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.common.ClientType;
import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.service.group.model.req.GroupMemberDto;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo){

        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberList(groupId, clientInfo.getAppId());

        for (String memberId : groupMemberId) {
            if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() &&
                    memberId.equals(userId)) {
                messageProducer.sendToUserClientsExceptOne(memberId, command, data, clientInfo);
            } else{
                messageProducer.sendToUserClients(memberId, command, data, clientInfo.getAppId());
            }
        }
    }

}
