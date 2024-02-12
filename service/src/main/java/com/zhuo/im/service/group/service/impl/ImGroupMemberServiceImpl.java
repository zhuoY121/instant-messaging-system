package com.zhuo.im.service.group.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuo.im.codec.pack.group.AddGroupMemberPack;
import com.zhuo.im.codec.pack.group.DeleteGroupMemberPack;
import com.zhuo.im.codec.pack.group.UpdateGroupMemberPack;
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
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.dao.ImGroupMemberEntity;
import com.zhuo.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.zhuo.im.service.group.model.callback.AddGroupMemberAfterCallback;
import com.zhuo.im.service.group.model.req.*;
import com.zhuo.im.service.group.model.resp.AddMemberResp;
import com.zhuo.im.service.group.model.resp.GetRoleInGroupResp;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import com.zhuo.im.service.group.service.ImGroupService;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.service.ImUserService;
import com.zhuo.im.service.utils.CallbackService;
import com.zhuo.im.service.utils.GroupMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @description:
 * @version: 1.0
 */
@Service
@Slf4j
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    @Autowired
    ImGroupMemberMapper imGroupMemberMapper;

    @Autowired
    ImGroupService groupService;

    @Autowired
    ImGroupMemberService thisService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Override
    public ResponseVO importGroupMember(ImportGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        for (GroupMemberDto memberDto : req.getMemberList()) {
            ResponseVO responseVO;
            try {
                responseVO = thisService.addGroupMember(req.getGroupId(), req.getAppId(), memberDto);
            } catch (Exception e) {
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse();
            }

            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberDto.getMemberId());
            if (responseVO.isOk()) {
                addMemberResp.setResult(0);
            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_IN_GROUP.getCode()) {
                addMemberResp.setResult(2);
            } else {
                addMemberResp.setResult(1);
            }
            resp.add(addMemberResp);
        }

        return ResponseVO.successResponse(resp);
    }

    /**
     * @param
     * @return
     * @description: Add group members, only for internal calls
     */
    @Override
    @Transactional
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto) {

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(dto.getMemberId(), appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        if (dto.getRole() != null && GroupMemberRoleEnum.OWNER.getCode() == dto.getRole()) {
            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id", groupId);
            queryOwner.eq("app_id", appId);
            queryOwner.eq("role", GroupMemberRoleEnum.OWNER.getCode());
            Integer ownerNum = imGroupMemberMapper.selectCount(queryOwner);
            if (ownerNum > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_OWNER_EXIST);
            }
        }

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        query.eq("app_id", appId);
        query.eq("member_id", dto.getMemberId());
        ImGroupMemberEntity memberDto = imGroupMemberMapper.selectOne(query);

        long now = System.currentTimeMillis();
        if (memberDto == null) {
            // Joining the group for the first time
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setGroupId(groupId);
            memberDto.setAppId(appId);
            memberDto.setJoinTime(now);
            int insert = imGroupMemberMapper.insert(memberDto);
            if (insert == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        } else if (GroupMemberRoleEnum.LEFT.getCode() == memberDto.getRole()) {
            // Join the group again
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setJoinTime(now);
            int update = imGroupMemberMapper.update(memberDto, query);
            if (update == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_IN_GROUP);

    }

    /**
     * @description: The logic of adding group members, adding people into the group, and entering the group chat directly.
     * If you are an APP administrator, you can directly join the group.
     * Otherwise, only private groups can call this interface, and group members can also bring people into the group.
     * Only private groups can call this interface.
     * @param req
     * @return
     */
    @Override
    public ResponseVO addGroupMember(AddGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        List<GroupMemberDto> memberDtos = req.getMemberList();

        // Before callback
        if (appConfig.isAddGroupMemberBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.AddGroupMemberBefore,
                    JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }

            try {
                memberDtos = JSONArray.parseArray(JSONObject.toJSONString(responseVO.getData()), GroupMemberDto.class);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("AddGroupMemberBefore callback failed: appId={}", req.getAppId());
            }
        }

        ImGroupEntity group = groupResp.getData();

        /**
         * Private group (private) is similar to an ordinary WeChat group. After creation, only friends who are already in the group can be invited to join the group without the consent of the invited party or the approval of the group owner.
         * Public group (Public) is similar to QQ group. After creation, the group owner can designate the group managers. Approval by the group owner or managers is required before joining the group.
         * Group type: 1=private group (similar to WeChat); 2=public group (similar to QQ)
         */
        if (!isAdmin && GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
            throw new ApplicationException(GroupErrorCode.NEED_APP_ADMIN_ROLE);
        }

        List<String> successId = new ArrayList<>();
        for (GroupMemberDto memberDto : memberDtos) {
            ResponseVO responseVO = null;
            try {
                responseVO = thisService.addGroupMember(req.getGroupId(), req.getAppId(), memberDto);
            } catch (Exception e) {
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse();
            }
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberDto.getMemberId());
            if (responseVO.isOk()) {
                successId.add(memberDto.getMemberId());
                addMemberResp.setResult(0);
            } else if (responseVO.getCode() == GroupErrorCode.USER_IS_IN_GROUP.getCode()) {
                addMemberResp.setResult(2);
                addMemberResp.setResultMessage(responseVO.getMsg());
            } else {
                addMemberResp.setResult(1);
                addMemberResp.setResultMessage(responseVO.getMsg());
            }
            resp.add(addMemberResp);
        }

        // Send tcp notification
        AddGroupMemberPack pack = new AddGroupMemberPack();
        pack.setGroupId(req.getGroupId());
        pack.setMembers(successId);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.ADD_MEMBER, pack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        // After callback
        if (appConfig.isAddGroupMemberAfterCallback()) {
            AddGroupMemberAfterCallback callbackDto = new AddGroupMemberAfterCallback();
            callbackDto.setGroupId(req.getGroupId());
            callbackDto.setGroupType(group.getGroupType());
            callbackDto.setMemberRespList(resp);
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.AddGroupMemberAfter,
                    JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroup(String groupId, String memberId, Integer appId) {

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        query.eq("app_id", appId);
        query.eq("member_id", memberId);

        ImGroupMemberEntity imGroupMemberEntity = imGroupMemberMapper.selectOne(query);
        if (imGroupMemberEntity == null || imGroupMemberEntity.getRole() == GroupMemberRoleEnum.LEFT.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_IN_GROUP);
        }

        GetRoleInGroupResp resp = new GetRoleInGroupResp();
        resp.setSpeakDate(imGroupMemberEntity.getSpeakDate());
        resp.setGroupMemberId(imGroupMemberEntity.getGroupMemberId());
        resp.setMemberId(imGroupMemberEntity.getMemberId());
        resp.setRole(imGroupMemberEntity.getRole());
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId) {
        List<GroupMemberDto> groupMember = imGroupMemberMapper.getGroupMember(appId, groupId);
        return ResponseVO.successResponse(groupMember);
    }

    @Override
    public List<String> getGroupMemberIdList(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupMemberIdList(appId, groupId);
    }

    @Override
    public List<GroupMemberDto> getGroupManagerList(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupManager(groupId, appId);
    }

    @Override
    public List<String> getGroupMemberList(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupMemberId(appId, groupId);
    }

    @Override
    public ResponseVO<Collection<String>> getJoinedGroupByMember(GetJoinedGroupReq req) {

        if (req.getLimit() != null) {
            Page<ImGroupMemberEntity> objectPage = new Page<>(req.getOffset(), req.getLimit());
            QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("member_id", req.getMemberId());
            IPage<ImGroupMemberEntity> imGroupMemberEntityPage = imGroupMemberMapper.selectPage(objectPage, query);

            Set<String> groupId = new HashSet<>();
            List<ImGroupMemberEntity> records = imGroupMemberEntityPage.getRecords();
            records.forEach(e -> {
                groupId.add(e.getGroupId());
            });

            return ResponseVO.successResponse(groupId);
        } else {
            return ResponseVO.successResponse(imGroupMemberMapper.getJoinedGroupId(req.getAppId(), req.getMemberId()));
        }
    }

    @Override
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId) {

        // Update old group owner
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", appId);
        updateWrapper.eq("group_id", groupId);
        updateWrapper.eq("role", GroupMemberRoleEnum.OWNER.getCode());
        imGroupMemberMapper.update(imGroupMemberEntity, updateWrapper);

        // Update new group owner
        ImGroupMemberEntity newOwner = new ImGroupMemberEntity();
        newOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        UpdateWrapper<ImGroupMemberEntity> ownerWrapper = new UpdateWrapper<>();
        ownerWrapper.eq("app_id", appId);
        ownerWrapper.eq("group_id", groupId);
        ownerWrapper.eq("member_id", owner);
        imGroupMemberMapper.update(newOwner, ownerWrapper);

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO removeGroupMember(RemoveGroupMemberReq req) {

        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        ImGroupEntity group = groupResp.getData();

        if (!isAdmin) {
            if (GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {

                // Obtain the permission of the operator. The administrator or group leader or group member
                ResponseVO<GetRoleInGroupResp> role = getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
                if (!role.isOk()) {
                    return role;
                }

                GetRoleInGroupResp data = role.getData();
                Integer roleInfo = data.getRole();

                boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = roleInfo == GroupMemberRoleEnum.MANAGER.getCode();

                if (!isOwner && !isManager) {
                    throw new ApplicationException(GroupErrorCode.NEED_OWNER_OR_MANAGER_ROLE);
                }

                // In a private group, you must be the group leader to remove members.
                if (!isOwner && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()) {
                    throw new ApplicationException(GroupErrorCode.NEED_OWNER_ROLE);
                }

                // In public groups, managers and group owners can remove people, but managers can only remove ordinary group members.
                if (GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
                    // throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                    // Get permissions of people removed from group chat
                    ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
                    if (!roleInGroupOne.isOk()) {
                        return roleInGroupOne;
                    }
                    GetRoleInGroupResp memberRole = roleInGroupOne.getData();
                    if (memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                        throw new ApplicationException(GroupErrorCode.CANNOT_REMOVE_GROUP_OWNER);
                    }
                    // If the operator is a manager and the person removed from the group chat is not an ordinary group member (owner or manager), the operation cannot be performed.
                    // Because the group manager can only remove ordinary group members.
                    if (isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                        throw new ApplicationException(GroupErrorCode.NEED_OWNER_ROLE);
                    }
                }
            }
        }

        ResponseVO responseVO = thisService.removeGroupMember(req.getGroupId(), req.getAppId(), req.getMemberId());

        if (responseVO.isOk()) {
            // Send tcp notification
            DeleteGroupMemberPack pack = new DeleteGroupMemberPack();
            pack.setGroupId(req.getGroupId());
            pack.setMember(req.getMemberId());
            groupMessageProducer.producer(req.getOperator(), GroupEventCommand.DELETE_MEMBER, pack,
                    new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

            // After callback
            if (appConfig.isDeleteGroupMemberAfterCallback()) {
                callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteGroupMemberAfter,
                        JSONObject.toJSONString(req));
            }
        }

        return responseVO;
    }

    /**
     * @description: Delete group members, only for internal calls
     * @return
     */
    @Override
    @Transactional
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId) {

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(memberId, appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = getRoleInGroup(groupId, memberId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        GetRoleInGroupResp data = roleInGroupOne.getData();
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.LEFT.getCode());
        imGroupMemberEntity.setLeaveTime(System.currentTimeMillis());
        imGroupMemberEntity.setGroupMemberId(data.getGroupMemberId());
        imGroupMemberMapper.updateById(imGroupMemberEntity);
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req) {

        boolean isAdmin = false;

        ResponseVO<ImGroupEntity> group = groupService.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        ImGroupEntity groupData = group.getData();
        if (groupData.getStatus() == GroupStatusEnum.DISBANDED.getCode()) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DISBANDED);
        }

        // Whether the current operator is the target user.
        boolean isMyself = req.getOperator().equals(req.getMemberId());

        if (!isAdmin) {
            // Other people cannot change your group alias. Only you can change your group alias.
            if (!StringUtils.isBlank(req.getAlias()) && !isMyself) {
                return ResponseVO.errorResponse(GroupErrorCode.NEED_YOURSELF);
            }

            // If you want to modify permissions, follow the following logic
            if (req.getRole() != null) {
                // Private groups cannot set up managers/owners (TBD)
                if (groupData.getGroupType() == GroupTypeEnum.PRIVATE.getCode() &&
                        req.getRole() != null &&
                        (req.getRole() == GroupMemberRoleEnum.MANAGER.getCode() || req.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
                    return ResponseVO.errorResponse(GroupErrorCode.NEED_APP_ADMIN_ROLE);
                }

                // Get the role of the target person.
                ResponseVO<GetRoleInGroupResp> roleInGroup = this.getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
                if (!roleInGroup.isOk()) {
                    return roleInGroup;
                }

                // Get the role of the current operator.
                ResponseVO<GetRoleInGroupResp> operatorRoleInGroup = this.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
                if (!operatorRoleInGroup.isOk()) {
                    return operatorRoleInGroup;
                }

                GetRoleInGroupResp data = operatorRoleInGroup.getData();
                Integer roleInfo = data.getRole();
                boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = roleInfo == GroupMemberRoleEnum.MANAGER.getCode();

                // Only group owners or managers can modify permissions
                if (req.getRole() != null && !isOwner && !isManager) {
                    return ResponseVO.errorResponse(GroupErrorCode.NEED_OWNER_OR_MANAGER_ROLE);
                }

                // Only the group owner can set up managers
                if (req.getRole() != null && req.getRole() == GroupMemberRoleEnum.MANAGER.getCode() && !isOwner) {
                    return ResponseVO.errorResponse(GroupErrorCode.NEED_OWNER_ROLE);
                }

            }
        }

        ImGroupMemberEntity update = new ImGroupMemberEntity();

        if (StringUtils.isNotBlank(req.getAlias())) {
            update.setAlias(req.getAlias());
        }

        // It is not allowed to directly specify the group owner through this interface.
        // The group owner can only be designated through the transfer interface.
        if (req.getRole() != null) {
            if (req.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                throw new ApplicationException(GroupErrorCode.CANNOT_SET_GROUP_OWNER);
            } else {
                update.setRole(req.getRole());
            }
        }

        UpdateWrapper<ImGroupMemberEntity> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("app_id", req.getAppId());
        objectUpdateWrapper.eq("member_id", req.getMemberId());
        objectUpdateWrapper.eq("group_id", req.getGroupId());
        imGroupMemberMapper.update(update, objectUpdateWrapper);

        // Send tcp notification
        UpdateGroupMemberPack pack = new UpdateGroupMemberPack();
        pack.setGroupId(req.getGroupId());
        pack.setMemberId(req.getMemberId());
        pack.setAlias(req.getAlias());
        pack.setExtra(req.getExtra());
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.UPDATE_MEMBER, pack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO muteGroupMember(MuteGroupMemberReq req) {

        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        boolean isAdmin = false;

        GetRoleInGroupResp memberRole = null;

        if (!isAdmin) {

            // Obtain the permission of the operator
            ResponseVO<GetRoleInGroupResp> role = getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
            boolean isManager = roleInfo == GroupMemberRoleEnum.MANAGER.getCode();

            if (!isOwner && !isManager) {
                throw new ApplicationException(GroupErrorCode.NEED_OWNER_OR_MANAGER_ROLE);
            }

            // Obtain the permissions of the target person
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }
            memberRole = roleInGroupOne.getData();

            // Only the APP admin can mute the group owner.
            if (memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                throw new ApplicationException(GroupErrorCode.NEED_APP_ADMIN_ROLE);
            }

            // Group managers can only mute ordinary users.
            if (isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                throw new ApplicationException(GroupErrorCode.NEED_OWNER_ROLE);
            }
        }

        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        if (memberRole == null) {
            // Obtain the permissions of the target person
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }
            memberRole = roleInGroupOne.getData();
        }

        imGroupMemberEntity.setGroupMemberId(memberRole.getGroupMemberId());
        if (req.getMuteDuration() > 0) {
            imGroupMemberEntity.setSpeakDate(System.currentTimeMillis() + req.getMuteDuration());
        } else {
            imGroupMemberEntity.setSpeakDate(req.getMuteDuration());
        }

        int i = imGroupMemberMapper.updateById(imGroupMemberEntity);
        return ResponseVO.successResponse();
    }


}
