package com.zhuo.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.ApproveFriendRequestStatusEnum;
import com.zhuo.im.common.enums.FriendshipErrorCode;
import com.zhuo.im.service.friendship.dao.ImFriendshipRequestEntity;
import com.zhuo.im.service.friendship.dao.mapper.ImFriendshipRequestMapper;
import com.zhuo.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.zhuo.im.service.friendship.model.req.FriendshipDto;
import com.zhuo.im.service.friendship.model.req.GetFriendshipRequestReq;
import com.zhuo.im.service.friendship.model.req.ReadFriendshipRequestReq;
import com.zhuo.im.service.friendship.service.ImFriendshipRequestService;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImFriendshipRequestServiceImpl implements ImFriendshipRequestService {

    @Autowired
    ImFriendshipRequestMapper imFriendshipRequestMapper;

    @Autowired
    ImFriendshipService imFriendshipService;

    @Override
    public ResponseVO addFriendshipRequest(String fromId, FriendshipDto dto, Integer appId) {

        QueryWrapper<ImFriendshipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("to_id", dto.getToId());
        ImFriendshipRequestEntity request = imFriendshipRequestMapper.selectOne(queryWrapper);
        if (request == null) {
            request.setAddSource(dto.getAddSource());
            request.setAddMessage(dto.getAddMessage());
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            imFriendshipRequestMapper.insert(request);

        } else {
            // modify content and update time
            if (StringUtils.isNotBlank(dto.getAddSource())) {
                request.setAddMessage(dto.getAddMessage());
            }
            if (StringUtils.isNotBlank(dto.getRemark())) {
                request.setRemark(dto.getRemark());
            }
            if (StringUtils.isNotBlank(dto.getAddMessage())) {
                request.setAddMessage(dto.getAddMessage());
            }

            imFriendshipRequestMapper.updateById(request);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO approveFriendRequest(ApproveFriendRequestReq req) {

        ImFriendshipRequestEntity imFriendShipRequestEntity = imFriendshipRequestMapper.selectById(req.getId());
        if(imFriendShipRequestEntity == null){
            return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_REQUEST_NOT_EXIST);
        }

        if(!req.getOperator().equals(imFriendShipRequestEntity.getToId())){
            // You can only process friend requests sent to yourself
            return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_REQUEST_RECIPIENT_NOT_MATCHED);
        }

        ImFriendshipRequestEntity update = new ImFriendshipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setId(req.getId());
        imFriendshipRequestMapper.updateById(update);

        if (ApproveFriendRequestStatusEnum.ACCEPT.getCode() == req.getStatus()){
            // Accept ===> To execute the logic of adding friends
            FriendshipDto dto = new FriendshipDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddMessage(imFriendShipRequestEntity.getAddMessage());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO = imFriendshipService.doAddFriendship(imFriendShipRequestEntity.getFromId(), dto,req.getAppId());

            if(!responseVO.isOk() && responseVO.getCode() != FriendshipErrorCode.TO_IS_YOUR_FRIEND.getCode()){
                return responseVO;
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO readFriendshipRequestReq(ReadFriendshipRequestReq req) {

        QueryWrapper<ImFriendshipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId()); // NOTE: Approve of all requests sent to myself, so the "to_id" of the request table is equal to myself of the calling interface

        ImFriendshipRequestEntity update = new ImFriendshipRequestEntity();
        update.setReadStatus(1);
        imFriendshipRequestMapper.update(update, query);

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getFriendRequest(GetFriendshipRequestReq req) {

        QueryWrapper<ImFriendshipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());

        List<ImFriendshipRequestEntity> requestList = imFriendshipRequestMapper.selectList(query);

        return ResponseVO.successResponse(requestList);
    }
}
