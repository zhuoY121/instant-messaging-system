package com.zhuo.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.FriendshipErrorCode;
import com.zhuo.im.common.enums.FriendshipStatusEnum;
import com.zhuo.im.service.friendship.dao.ImFriendshipEntity;
import com.zhuo.im.service.friendship.dao.mapper.ImFriendshipMapper;
import com.zhuo.im.service.friendship.model.req.*;
import com.zhuo.im.service.friendship.model.resp.ImportFriendshipResp;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.service.ImUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class ImFriendshipServiceImpl implements ImFriendshipService {

    @Autowired
    ImFriendshipMapper imFriendshipMapper;

    @Autowired
    ImUserService imUserService;

    @Override
    public ResponseVO importFriendship(ImportFriendshipReq req) {

        if(req.getFriendItem().size() > 100){
            return ResponseVO.errorResponse(FriendshipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendshipResp resp = new ImportFriendshipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImportFriendshipReq.ImportFriendDto dto:
             req.getFriendItem()) {

            ImFriendshipEntity entity = new ImFriendshipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());

            try {
                int insert = imFriendshipMapper.insert(entity);
                if(insert == 1){
                    successId.add(dto.getToId());
                }else{
                    errorId.add(dto.getToId());
                }
            }catch (Exception e){
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriendship(AddFriendshipReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        return this.doAddFriendship(req.getFromId(), req.getToItem(), req.getAppId());
    }

    @Override
    public ResponseVO updateFriendship(UpdateFriendshipReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        return this.doUpdateFriendship(req.getFromId(), req.getToItem(), req.getAppId());
    }

    @Override
    public ResponseVO deleteFriendship(DeleteFriendshipReq req) {

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendshipEntity fromItem = imFriendshipMapper.selectOne(query);

        if (fromItem == null) {
            return ResponseVO.errorResponse(FriendshipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (fromItem.getStatus() == FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ImFriendshipEntity update = new ImFriendshipEntity();
                update.setStatus(FriendshipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendshipMapper.update(update, query);
            } else {
                return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_IS_DELETED);
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriendship(DeleteAllFriendshipReq req) {

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("status", FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendshipEntity update = new ImFriendshipEntity();
        update.setStatus(FriendshipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendshipMapper.update(update, query);
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getFriendship(GetFriendshipReq req) {

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendshipEntity entity = imFriendshipMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendshipErrorCode.RELATIONSHIP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO getAllFriendship(GetAllFriendshipReq req) {

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        return ResponseVO.successResponse(imFriendshipMapper.selectList(query));
    }

    @Transactional
    public ResponseVO doUpdateFriendship(String fromId, FriendshipDto dto, Integer appId) {

        UpdateWrapper<ImFriendshipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendshipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendshipEntity::getExtra, dto.getExtra())
                .set(ImFriendshipEntity::getRemark, dto.getRemark())
                .eq(ImFriendshipEntity::getAppId, appId)
                .eq(ImFriendshipEntity::getFromId, fromId)
                .eq(ImFriendshipEntity::getToId, dto.getToId());

        int update = imFriendshipMapper.update(null, updateWrapper);
        if(update != 1){
            return ResponseVO.errorResponse();
        }
        return ResponseVO.successResponse();
    }

    @Transactional
    public ResponseVO doAddFriendship(String fromId, FriendshipDto dto, Integer appId) {

        // A-B
        // Insert two records A and B into the Friends table
        // Check whether there are records. If it exists, determine the status.
        // If it has been added, it will prompt that it has been added; if it has not been added, modify the status.

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendshipEntity fromItem = imFriendshipMapper.selectOne(query);

        if (fromItem == null) {
            // add friend
            fromItem = new ImFriendshipEntity();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());

            int insert = imFriendshipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendshipErrorCode.ADD_FRIEND_ERROR);
            }

        } else {
            // If it exists, determine the status.
            // If it has been added, it will prompt that it has been added; if it has not been added, modify the status.
            if (fromItem.getStatus() == FriendshipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendshipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                ImFriendshipEntity update = new ImFriendshipEntity();

                if(StringUtils.isNotBlank(dto.getAddSource())){
                    update.setAddSource(dto.getAddSource());
                }

                if(StringUtils.isNotBlank(dto.getRemark())){
                    update.setRemark(dto.getRemark());
                }

                if(StringUtils.isNotBlank(dto.getExtra())){
                    update.setExtra(dto.getExtra());
                }
                update.setStatus(FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendshipMapper.update(update, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendshipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }

        return ResponseVO.successResponse();
    }
}
