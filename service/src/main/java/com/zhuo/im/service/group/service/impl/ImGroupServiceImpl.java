package com.zhuo.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.GroupErrorCode;
import com.zhuo.im.common.enums.GroupStatusEnum;
import com.zhuo.im.common.enums.GroupTypeEnum;
import com.zhuo.im.common.exception.ApplicationException;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.dao.mapper.ImGroupMapper;
import com.zhuo.im.service.group.model.req.ImportGroupReq;
import com.zhuo.im.service.group.service.ImGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupMapper;


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



}
