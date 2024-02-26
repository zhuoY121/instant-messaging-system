package com.zhuo.im.service.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuo.im.codec.pack.group.*;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.GroupErrorCode;
import com.zhuo.im.common.enums.GroupMemberRoleEnum;
import com.zhuo.im.common.enums.GroupStatusEnum;
import com.zhuo.im.common.enums.GroupTypeEnum;
import com.zhuo.im.common.enums.command.GroupEventCommand;
import com.zhuo.im.common.exception.ApplicationException;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.service.friendship.model.callback.DeleteFriendBlackAfterCallbackDto;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.dao.mapper.ImGroupMapper;
import com.zhuo.im.service.group.model.callback.DeleteGroupCallbackDto;
import com.zhuo.im.service.group.model.req.*;
import com.zhuo.im.service.group.model.resp.GetGroupResp;
import com.zhuo.im.service.group.model.resp.GetJoinedGroupResp;
import com.zhuo.im.service.group.model.resp.GetRoleInGroupResp;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import com.zhuo.im.service.group.service.ImGroupService;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.utils.CallbackService;
import com.zhuo.im.service.utils.GroupMessageProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupMapper;

    @Autowired
    ImGroupMemberService groupMemberService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Autowired
    RedisSeq redisSeq;


    @Override
    public ResponseVO importGroup(ImportGroupReq req) {

        // 1. Determine whether the group id exists
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_EXIST);
            }
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();

        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        if (req.getCreateTime() == null) {
            imGroupEntity.setCreateTime(System.currentTimeMillis());
        }
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);

        int insert = imGroupMapper.insert(imGroupEntity);
        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false; // TODO: add admin role later

        if (!isAdmin) {
            req.setOwnerId(req.getOperator());
        }

        // 1. Determine whether the group id exists
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_EXIST);
            }
        }

        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        imGroupEntity.setCreateTime(System.currentTimeMillis());
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        imGroupEntity.setSequence(seq);
        int insert = imGroupMapper.insert(imGroupEntity);

        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.setMemberId(req.getOwnerId());
        groupMemberDto.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupMemberDto.setJoinTime(System.currentTimeMillis());
        groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupMemberDto);

        // Insert group members
        for (GroupMemberDto dto : req.getMemberList()) {
            groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
        }

        // Send tcp notification
        CreateGroupPack createGroupPack = new CreateGroupPack();
        BeanUtils.copyProperties(imGroupEntity, createGroupPack);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.CREATE_GROUP, createGroupPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        // After callback
        if (appConfig.isCreateGroupAfterCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.CreateGroupAfter,
                    JSONObject.toJSONString(imGroupEntity));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String groupId, Integer appId) {

        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(query);

        if (imGroupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_NOT_EXIST);
        }
        return ResponseVO.successResponse(imGroupEntity);
    }


    /**
     * @param
     * @return
     * @description Modify the basic information of the group. If it is called by the background administrator, the permissions will not be checked.
     * If not, the permissions will be checked. If it is a private group (WeChat group), anyone can modify the information.
     * In a public group, only the administrator can modify it.
     * The group owner or administrator can modify other information.
     */
    @Override
    @Transactional
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req) {

        // 1. Determine whether the group id exists
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("group_id", req.getGroupId());
        query.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(query);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_EXIST);
        }

        if(imGroupEntity.getStatus() == GroupStatusEnum.DISBANDED.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DISBANDED);
        }

        boolean isAdmin = false;    // TODO: add admin role later

        if (!isAdmin) {
            // If the operator is not the admin, then it requires checking permissions
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MANAGER.getCode();
            boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            // In public groups, only the group manager OR group owner can modify the information.
            if (!isManager && !isOwner && GroupTypeEnum.PUBLIC.getCode() == imGroupEntity.getGroupType()) {
                throw new ApplicationException(GroupErrorCode.NEED_OWNER_OR_MANAGER_ROLE);
            }

        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);

        ImGroupEntity update = new ImGroupEntity();
        BeanUtils.copyProperties(req, update);
        update.setUpdateTime(System.currentTimeMillis());
        update.setSequence(seq);
        int row = imGroupMapper.update(update, query);
        if (row != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        // Send tcp notification
        UpdateGroupInfoPack pack = new UpdateGroupInfoPack();
        BeanUtils.copyProperties(req, pack);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.UPDATE_GROUP,
                pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        // After callback
        if (appConfig.isModifyGroupAfterCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.UpdateGroupAfter,
                    JSONObject.toJSONString(imGroupMapper.selectOne(query)));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(GetGroupReq req) {

        ResponseVO group = this.getGroup(req.getGroupId(), req.getAppId());

        if(!group.isOk()){
            return group;
        }

        GetGroupResp getGroupResp = new GetGroupResp();
        BeanUtils.copyProperties(group.getData(), getGroupResp);
        try {
            ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
            if (groupMember.isOk()) {
                getGroupResp.setMemberList(groupMember.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseVO.successResponse(getGroupResp);
    }

    @Override
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req) {

        // Get joined group IDs
        ResponseVO<Collection<String>> joinedGroupByMember = groupMemberService.getJoinedGroupByMember(req);
        if (!joinedGroupByMember.isOk()) {
            return joinedGroupByMember;
        }

        GetJoinedGroupResp resp = new GetJoinedGroupResp();
        if (CollectionUtils.isEmpty(joinedGroupByMember.getData())) {
            resp.setTotalCount(0);
            resp.setGroupList(new ArrayList<>());
            return ResponseVO.successResponse(resp);

        } else {
            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.in("group_id", joinedGroupByMember.getData());
            if (!CollectionUtils.isEmpty(req.getGroupType())) {
                query.in("group_type", req.getGroupType());
            }

            List<ImGroupEntity> groupList = imGroupMapper.selectList(query);
            resp.setGroupList(groupList);
            if (req.getLimit() == null) {
                resp.setTotalCount(groupList.size());
            } else {
                resp.setTotalCount(imGroupMapper.selectCount(query));
            }

            // Get group info by group id
            return ResponseVO.successResponse(groupList);
        }

    }

    /**
     * @description Disband the group. Only the backend administrator and group owner can disband the group
     * @param req
     * @return
     */
    @Override
    @Transactional
    public ResponseVO deleteGroup(DeleteGroupReq req) {

        boolean isAdmin = false;

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(objectQueryWrapper);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.CANNOT_DISBAND_PRIVATE_GROUP);
        }

        if(imGroupEntity.getStatus() == GroupStatusEnum.DISBANDED.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DISBANDED);
        }

        if (!isAdmin) {
            if (imGroupEntity.getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
                throw new ApplicationException(GroupErrorCode.NEED_OWNER_ROLE);
            }

            if (imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                    !imGroupEntity.getOwnerId().equals(req.getOperator())) {
                throw new ApplicationException(GroupErrorCode.NEED_OWNER_ROLE);
            }
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);

        ImGroupEntity update = new ImGroupEntity();
        update.setStatus(GroupStatusEnum.DISBANDED.getCode());
        update.setSequence(seq);
        int update1 = imGroupMapper.update(update, objectQueryWrapper);
        if (update1 != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        // Send tcp notification
        DeleteGroupPack pack = new DeleteGroupPack();
        pack.setGroupId(req.getGroupId());
        pack.setSequence(seq);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.DELETE_GROUP, pack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        // After callback
        if (appConfig.isDeleteGroupAfterCallback()) {
            DeleteGroupCallbackDto callbackDto = new DeleteGroupCallbackDto();
            callbackDto.setGroupId(req.getGroupId());
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteGroupAfter,
                    JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = groupMemberService.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        if (roleInGroupOne.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.NEED_OWNER_ROLE);
        }

        ResponseVO<GetRoleInGroupResp> newOwnerRole = groupMemberService.getRoleInGroup(req.getGroupId(), req.getOwnerId(), req.getAppId());
        if (!newOwnerRole.isOk()) {
            return newOwnerRole;
        }

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(objectQueryWrapper);
        if(imGroupEntity.getStatus() == GroupStatusEnum.DISBANDED.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DISBANDED);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);

        ImGroupEntity updateGroup = new ImGroupEntity();
        updateGroup.setOwnerId(req.getOwnerId());
        updateGroup.setSequence(seq);
        UpdateWrapper<ImGroupEntity> updateGroupWrapper = new UpdateWrapper<>();
        updateGroupWrapper.eq("app_id", req.getAppId());
        updateGroupWrapper.eq("group_id", req.getGroupId());
        imGroupMapper.update(updateGroup, updateGroupWrapper);
        groupMemberService.transferGroupMember(req.getOwnerId(), req.getGroupId(), req.getAppId());

        // Send tcp notification
        TransferGroupPack transferGroupPack = new TransferGroupPack();
        transferGroupPack.setGroupId(imGroupEntity.getGroupId());
        transferGroupPack.setOwnerId(imGroupEntity.getOwnerId());
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.TRANSFER_GROUP, transferGroupPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO muteGroup(MuteGroupReq req) {

        ResponseVO<ImGroupEntity> groupResp = getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        if(groupResp.getData().getStatus() == GroupStatusEnum.DISBANDED.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DISBANDED);
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            // Check permissions
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
            boolean isManager = roleInfo == GroupMemberRoleEnum.MANAGER.getCode();

            // In public groups, only the group owner or group managers can modify information.
            if (!isOwner && !isManager) {
                throw new ApplicationException(GroupErrorCode.NEED_OWNER_OR_MANAGER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();
        update.setMute(req.getMuted());

        UpdateWrapper<ImGroupEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id",req.getGroupId());
        wrapper.eq("app_id",req.getAppId());
        imGroupMapper.update(update,wrapper);

        // Send tcp notification
        MuteGroupPack pack = new MuteGroupPack();
        pack.setGroupId(req.getGroupId());
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.MUTE_GROUP, pack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }


}
