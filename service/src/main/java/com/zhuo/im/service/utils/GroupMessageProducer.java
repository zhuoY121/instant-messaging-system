package com.zhuo.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.codec.pack.group.AddGroupMemberPack;
import com.zhuo.im.codec.pack.group.DeleteGroupMemberPack;
import com.zhuo.im.codec.pack.group.UpdateGroupMemberPack;
import com.zhuo.im.common.ClientType;
import com.zhuo.im.common.enums.command.Command;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.service.group.model.req.GroupMemberDto;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        List<String> groupMemberIdList = imGroupMemberService.getGroupMemberList(groupId, clientInfo.getAppId());

        if (command.equals(GroupEventCommand.ADD_MEMBER)) {
            // Send to group managers and the user who is about to join the group
            List<GroupMemberDto> groupManagerDtoList = imGroupMemberService.getGroupManagerList(groupId, clientInfo.getAppId());
            AddGroupMemberPack addGroupMemberPack = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();

            List<String> groupManagerList = new ArrayList<>();
            for (GroupMemberDto memberDto : groupManagerDtoList) {
                groupManagerList.add(memberDto.getMemberId());
            }
            sendNotification(groupManagerList, clientInfo, userId, command, data);

            sendNotification(members, clientInfo, userId, command, data);

        } else if (command.equals(GroupEventCommand.DELETE_MEMBER)) {
            // Send to all group members and the person to be removed
            DeleteGroupMemberPack pack = o.toJavaObject(DeleteGroupMemberPack.class);
            String memberToBeRemoved = pack.getMember();

            List<String> memberList = imGroupMemberService.getGroupMemberIdList(groupId, clientInfo.getAppId());
            memberList.add(memberToBeRemoved);

            sendNotification(memberList, clientInfo, userId, command, data);

        } else if (command.equals(GroupEventCommand.UPDATE_MEMBER)) {
            // Send to group managers and the person whose info is about to be changed
            UpdateGroupMemberPack pack = o.toJavaObject(UpdateGroupMemberPack.class);
            String memberId = pack.getMemberId();

            List<GroupMemberDto> groupManagerList = imGroupMemberService.getGroupManagerList(groupId, clientInfo.getAppId());

            List<String> memberList = new ArrayList<>();
            memberList.add(memberId);
            for (GroupMemberDto memberDto : groupManagerList) {
                memberList.add(memberDto.getMemberId());
            }

            sendNotification(memberList, clientInfo, userId, command, data);

        } else {
            // Send to all group members
            sendNotification(groupMemberIdList, clientInfo, userId, command, data);
        }
    }

    private void sendNotification(List<String> memberIdList, ClientInfo clientInfo, String userId, Command command, Object data) {

        for (String memberId : memberIdList) {
            if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() &&
                    memberId.equals(userId)) {
                messageProducer.sendToUserClientsExceptOne(memberId, command, data, clientInfo);
            } else {
                messageProducer.sendToUserClients(memberId, command, data ,clientInfo.getAppId());
            }
        }
    }

}
