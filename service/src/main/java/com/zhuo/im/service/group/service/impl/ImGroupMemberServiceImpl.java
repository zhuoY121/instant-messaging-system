package com.zhuo.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.GroupErrorCode;
import com.zhuo.im.common.enums.GroupMemberRoleEnum;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.dao.ImGroupMemberEntity;
import com.zhuo.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.zhuo.im.service.group.model.req.GroupMemberDto;
import com.zhuo.im.service.group.model.req.ImportGroupMemberReq;
import com.zhuo.im.service.group.model.resp.AddMemberResp;
import com.zhuo.im.service.group.model.resp.GetRoleInGroupResp;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import com.zhuo.im.service.group.service.ImGroupService;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.service.ImUserService;
import lombok.extern.slf4j.Slf4j;
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


}
