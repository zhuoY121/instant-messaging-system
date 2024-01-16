package com.zhuo.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.GroupErrorCode;
import com.zhuo.im.common.enums.GroupMemberRoleEnum;
import com.zhuo.im.common.enums.GroupStatusEnum;
import com.zhuo.im.common.enums.GroupTypeEnum;
import com.zhuo.im.common.exception.ApplicationException;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.dao.mapper.ImGroupMapper;
import com.zhuo.im.service.group.model.req.*;
import com.zhuo.im.service.group.model.resp.GetGroupResp;
import com.zhuo.im.service.group.model.resp.GetRoleInGroupResp;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import com.zhuo.im.service.group.service.ImGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        imGroupEntity.setCreateTime(System.currentTimeMillis());
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
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

        if(imGroupEntity.getStatus() == GroupStatusEnum.DESTROY.getCode()){
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

        ImGroupEntity update = new ImGroupEntity();
        BeanUtils.copyProperties(req, update);
        update.setUpdateTime(System.currentTimeMillis());
        int row = imGroupMapper.update(update, query);
        if (row != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
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


}
