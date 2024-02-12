package com.zhuo.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.codec.pack.friendship.AddFriendGroupMemberPack;
import com.zhuo.im.codec.pack.friendship.DeleteFriendGroupMemberPack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.command.FriendshipEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.service.friendship.dao.ImFriendshipGroupEntity;
import com.zhuo.im.service.friendship.dao.ImFriendshipGroupMemberEntity;
import com.zhuo.im.service.friendship.dao.mapper.ImFriendshipGroupMemberMapper;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupMemberReq;
import com.zhuo.im.service.friendship.model.req.DeleteFriendshipGroupMemberReq;
import com.zhuo.im.service.friendship.service.ImFriendshipGroupMemberService;
import com.zhuo.im.service.friendship.service.ImFriendshipGroupService;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.service.ImUserService;
import com.zhuo.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 **/
@Service
public class ImFriendshipGroupMemberServiceImpl implements ImFriendshipGroupMemberService {

    @Autowired
    ImFriendshipGroupMemberMapper imFriendshipGroupMemberMapper;

    @Autowired
    ImFriendshipGroupService imFriendshipGroupService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendshipGroupMemberService thisService;

    @Autowired
    MessageProducer messageProducer;

    @Override
    @Transactional
    public ResponseVO addGroupMember(AddFriendshipGroupMemberReq req) {

        ResponseVO<ImFriendshipGroupEntity> group = imFriendshipGroupService
                .getGroup(req.getFromId(),req.getGroupName(),req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        List<String> successId = new ArrayList<>();
        for (String toId : req.getToIds()) {
            ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if (singleUserInfo.isOk()) {
                int i = thisService.doAddGroupMember(group.getData().getGroupId(), toId);
                if (i == 1) {
                    successId.add(toId);
                }
            }
        }

        // Send tcp notification
        AddFriendGroupMemberPack pack = new AddFriendGroupMemberPack();
        pack.setFromId(req.getFromId());
        pack.setGroupName(req.getGroupName());
        pack.setToIds(successId);
        messageProducer.sendToUserClientsExceptOne(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_ADD,
                pack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));

        return ResponseVO.successResponse(successId);
    }

    @Override
    public ResponseVO deleteGroupMember(DeleteFriendshipGroupMemberReq req) {
        ResponseVO<ImFriendshipGroupEntity> group = imFriendshipGroupService
                .getGroup(req.getFromId(),req.getGroupName(),req.getAppId());
        if(!group.isOk()){
            return group;
        }

        List<String> list = new ArrayList<>();
        for (String toId : req.getToIds()) {
            ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if(singleUserInfo.isOk()){
                int delete = this.doDeleteGroupMember(group.getData().getGroupId(), toId);
                if(delete == 1){
                    list.add(toId);
                }
            }
        }

        // Send tcp notification
        DeleteFriendGroupMemberPack pack = new DeleteFriendGroupMemberPack();
        pack.setFromId(req.getFromId());
        pack.setGroupName(req.getGroupName());
        pack.setToIds(list);
        messageProducer.sendToUserClientsExceptOne(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_DELETE,
                pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(list);
    }

    @Override
    public int doAddGroupMember(Long groupId, String toId) {
        ImFriendshipGroupMemberEntity imFriendshipGroupMemberEntity = new ImFriendshipGroupMemberEntity();
        imFriendshipGroupMemberEntity.setGroupId(groupId);
        imFriendshipGroupMemberEntity.setToId(toId);
        try {
            int insert = imFriendshipGroupMemberMapper.insert(imFriendshipGroupMemberEntity);
            return insert;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public int doDeleteGroupMember(Long groupId, String toId) {
        QueryWrapper<ImFriendshipGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id",groupId);
        queryWrapper.eq("to_id",toId);

        try {
            int delete = imFriendshipGroupMemberMapper.delete(queryWrapper);
            return delete;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int clearGroupMembers(Long groupId) {
        QueryWrapper<ImFriendshipGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id",groupId);
        int delete = imFriendshipGroupMemberMapper.delete(query);
        return delete;
    }
}
