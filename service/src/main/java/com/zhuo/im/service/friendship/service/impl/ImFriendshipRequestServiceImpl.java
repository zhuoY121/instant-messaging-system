package com.zhuo.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.codec.pack.friendship.ApproveFriendRequestPack;
import com.zhuo.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.ApproveFriendRequestStatusEnum;
import com.zhuo.im.common.enums.FriendshipErrorCode;
import com.zhuo.im.common.enums.command.FriendshipEventCommand;
import com.zhuo.im.service.friendship.dao.ImFriendshipRequestEntity;
import com.zhuo.im.service.friendship.dao.mapper.ImFriendshipRequestMapper;
import com.zhuo.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.zhuo.im.service.friendship.model.req.FriendshipDto;
import com.zhuo.im.service.friendship.model.req.GetFriendshipRequestReq;
import com.zhuo.im.service.friendship.model.req.ReadFriendshipRequestReq;
import com.zhuo.im.service.friendship.service.ImFriendshipRequestService;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.service.utils.WriteUserSeq;
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

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;


    @Override
    public ResponseVO addFriendshipRequest(String fromId, FriendshipDto dto, Integer appId) {

        QueryWrapper<ImFriendshipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("to_id", dto.getToId());
        ImFriendshipRequestEntity request = imFriendshipRequestMapper.selectOne(queryWrapper);

        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.FriendshipRequest);

        if (request == null) {
            request = new ImFriendshipRequestEntity();
            request.setAddSource(dto.getAddSource());
            request.setAddMessage(dto.getAddMessage());
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            request.setSequence(seq);
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
            request.setSequence(seq);

            imFriendshipRequestMapper.updateById(request);
        }

        writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.FriendshipRequest, seq);

        // A sends a friend request to B, then B will receive the TCP notification.
        messageProducer.sendToUserClients(dto.getToId(), null, "",
                FriendshipEventCommand.FRIEND_REQUEST, request, appId);

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

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipRequest);

        ImFriendshipRequestEntity update = new ImFriendshipRequestEntity();
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setId(req.getId());
        update.setSequence(seq);
        imFriendshipRequestMapper.updateById(update);

        writeUserSeq.writeUserSeq(req.getAppId(), req.getOperator(), Constants.SeqConstants.FriendshipRequest, seq);

        if (ApproveFriendRequestStatusEnum.ACCEPT.getCode() == req.getStatus()){
            // Accept ===> To execute the logic of adding friends
            FriendshipDto dto = new FriendshipDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddMessage(imFriendShipRequestEntity.getAddMessage());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO = imFriendshipService.doAddFriendship(req, imFriendShipRequestEntity.getFromId(), dto, req.getAppId());

            if(!responseVO.isOk() && responseVO.getCode() != FriendshipErrorCode.TO_IS_YOUR_FRIEND.getCode()){
                return responseVO;
            }
        }

        // Send TCP notification
        ApproveFriendRequestPack approveFriendRequestPack = new ApproveFriendRequestPack();
        approveFriendRequestPack.setId(req.getId());
        approveFriendRequestPack.setStatus(req.getStatus());
        approveFriendRequestPack.setSequence(seq);
        messageProducer.sendToUserClients(imFriendShipRequestEntity.getToId(),req.getClientType(),req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_APPROVE, approveFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO readFriendshipRequestReq(ReadFriendshipRequestReq req) {

        QueryWrapper<ImFriendshipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId()); // NOTE: Approve of all requests sent to myself, so the "to_id" of the request table is equal to myself of the calling interface

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipRequest);

        ImFriendshipRequestEntity update = new ImFriendshipRequestEntity();
        update.setReadStatus(1);
        update.setSequence(seq);
        imFriendshipRequestMapper.update(update, query);

        writeUserSeq.writeUserSeq(req.getAppId(), req.getOperator(), Constants.SeqConstants.FriendshipRequest, seq);

        // Send TCP notification
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        readAllFriendRequestPack.setSequence(seq);
        messageProducer.sendToUserClients(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_READ, readAllFriendRequestPack, req.getAppId());

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
