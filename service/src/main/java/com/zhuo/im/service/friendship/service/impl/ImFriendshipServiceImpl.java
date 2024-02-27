package com.zhuo.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.zhuo.im.codec.pack.friendship.*;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.AllowFriendTypeEnum;
import com.zhuo.im.common.enums.CheckFriendshipTypeEnum;
import com.zhuo.im.common.enums.FriendshipErrorCode;
import com.zhuo.im.common.enums.FriendshipStatusEnum;
import com.zhuo.im.common.enums.command.FriendshipEventCommand;
import com.zhuo.im.common.model.RequestBase;
import com.zhuo.im.common.model.SyncReq;
import com.zhuo.im.common.model.SyncResp;
import com.zhuo.im.service.friendship.dao.ImFriendshipEntity;
import com.zhuo.im.service.friendship.dao.mapper.ImFriendshipMapper;
import com.zhuo.im.service.friendship.model.callback.*;
import com.zhuo.im.service.friendship.model.req.*;
import com.zhuo.im.service.friendship.model.resp.CheckFriendshipResp;
import com.zhuo.im.service.friendship.model.resp.ImportFriendshipResp;
import com.zhuo.im.service.friendship.service.ImFriendshipRequestService;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.service.ImUserService;
import com.zhuo.im.service.utils.CallbackService;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    ImFriendshipRequestService imFriendshipRequestService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

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

        // Before callback
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.AddFriendBefore, JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }
        }

        ImUserDataEntity data = toInfo.getData();
        if (data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NO_NEED.getCode()) {
            return this.doAddFriendship(req, req.getFromId(), req.getToItem(), req.getAppId());
        } else {

            // Check whether the target is already your friend before sending AddFriendshipRequest.
            QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
            query.eq("app_id",req.getAppId());
            query.eq("from_id",req.getFromId());
            query.eq("to_id",req.getToItem().getToId());
            ImFriendshipEntity fromItem = imFriendshipMapper.selectOne(query);

            if (fromItem == null || fromItem.getStatus() != FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ResponseVO responseVO = imFriendshipRequestService.addFriendshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    return responseVO;
                }
            } else {
                return ResponseVO.errorResponse(FriendshipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }

        return this.doAddFriendship(req, req.getFromId(), req.getToItem(), req.getAppId());
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

        ResponseVO responseVO = doUpdateFriendship(req.getFromId(), req.getToItem(), req.getAppId());
        if (responseVO.isOk()) {
            // Send TCP notification
            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setRemark(req.getToItem().getRemark());
            updateFriendPack.setToId(req.getToItem().getToId());
            messageProducer.sendToUserClients(req.getFromId(), req.getClientType(), req.getImei(),
                    FriendshipEventCommand.FRIEND_UPDATE, updateFriendPack, req.getAppId());

            // After callback
            if (appConfig.isModifyFriendAfterCallback()) {
                UpdateFriendAfterCallbackDto callbackDto = new UpdateFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());

                callbackService.callback(req.getAppId(), Constants.CallbackCommand.UpdateFriendAfter, JSONObject.toJSONString(callbackDto));
            }
        }

        return responseVO;
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
            if (fromItem.getStatus() != null && fromItem.getStatus() == FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

                ImFriendshipEntity update = new ImFriendshipEntity();
                update.setStatus(FriendshipStatusEnum.FRIEND_STATUS_DELETED.getCode());
                update.setFriendSequence(seq);

                imFriendshipMapper.update(update, query);

                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);

                // Send TCP notification
                DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
                deleteFriendPack.setFromId(req.getFromId());
                deleteFriendPack.setToId(req.getToId());
                deleteFriendPack.setSequence(seq);
                messageProducer.sendToUserClients(req.getFromId(), req.getClientType(), req.getImei(),
                        FriendshipEventCommand.FRIEND_DELETE, deleteFriendPack, req.getAppId());

                // After callback
                if (appConfig.isDeleteFriendAfterCallback()) {
                    DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());

                    callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteFriendAfter, JSONObject.toJSONString(callbackDto));
                }

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
        update.setStatus(FriendshipStatusEnum.FRIEND_STATUS_DELETED.getCode());
        imFriendshipMapper.update(update, query);

        // Send TCP notification
        DeleteAllFriendPack deleteFriendPack = new DeleteAllFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        messageProducer.sendToUserClients(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_DELETE_ALL, deleteFriendPack, req.getAppId());

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

    @Override
    public ResponseVO checkFriendship(CheckFriendshipReq req) {

        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendshipResp> resp;

        if (req.getCheckType() == CheckFriendshipTypeEnum.SINGLE.getType()) {
            resp = imFriendshipMapper.checkFriendship(req);
        } else {
            resp = imFriendshipMapper.checkFriendshipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendshipResp::getToId
                        , CheckFriendshipResp::getStatus));

        for (String toId : result.keySet()){
            if(!collect.containsKey(toId)){
                CheckFriendshipResp checkFriendshipResp = new CheckFriendshipResp();
                checkFriendshipResp.setFromId(req.getFromId());
                checkFriendshipResp.setToId(toId);
                checkFriendshipResp.setStatus(result.get(toId));
                resp.add(checkFriendshipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addBlack(AddFriendshipBlackReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());
        query.eq("to_id",req.getToId());

        ImFriendshipEntity fromItem = imFriendshipMapper.selectOne(query);

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

        if (fromItem == null){
            // add friends
            fromItem = new ImFriendshipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setAppId(req.getAppId());
            fromItem.setBlack(FriendshipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            fromItem.setFriendSequence(seq);

            int insert = imFriendshipMapper.insert(fromItem);
            if (insert != 1){
                return ResponseVO.errorResponse(FriendshipErrorCode.ADD_BLACK_ERROR);
            }

            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);

        } else{
            // If it exists, determine the status. If it is blocked, it will prompt that it has been blocked. If it is not blocked, modify the status.
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendshipStatusEnum.BLACK_STATUS_BLACKED.getCode()){
                return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_IN_BLACKLIST);
            } else {
                ImFriendshipEntity update = new ImFriendshipEntity();
                update.setBlack(FriendshipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                update.setFriendSequence(seq);

                int result = imFriendshipMapper.update(update, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendshipErrorCode.ADD_BLACK_ERROR);
                }

                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);
            }
        }

        // Send TCP notification
        AddFriendBlacklistPack addFriendBlacklistPack = new AddFriendBlacklistPack();
        addFriendBlacklistPack.setFromId(req.getFromId());
        addFriendBlacklistPack.setToId(req.getToId());
        addFriendBlacklistPack.setSequence(seq);
        messageProducer.sendToUserClients(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACKLIST_ADD, addFriendBlacklistPack, req.getAppId());

        // After callback
        if (appConfig.isAddFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
            callbackDto.setFromId(req.getFromId());
            callbackDto.setToId(req.getToId());

            callbackService.callback(req.getAppId(), Constants.CallbackCommand.AddBlackAfter, JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {

        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendshipEntity fromItem = imFriendshipMapper.selectOne(query);

        if (fromItem == null) {
            return ResponseVO.errorResponse(FriendshipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }

        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendshipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_NOT_IN_BLACKLIST);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

        ImFriendshipEntity update = new ImFriendshipEntity();
        update.setBlack(FriendshipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        update.setFriendSequence(seq);

        int update1 = imFriendshipMapper.update(update, query);
        if(update1 == 1){
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Friendship, seq);

            // Send TCP notification
            DeleteFriendBlacklistPack deleteFriendBlacklistPack = new DeleteFriendBlacklistPack();
            deleteFriendBlacklistPack.setFromId(req.getFromId());
            deleteFriendBlacklistPack.setToId(req.getToId());
            deleteFriendBlacklistPack.setSequence(seq);
            messageProducer.sendToUserClients(req.getFromId(), req.getClientType(), req.getImei(),
                    FriendshipEventCommand.FRIEND_BLACKLIST_DELETE, deleteFriendBlacklistPack, req.getAppId());

            // After callback
            if (appConfig.isDeleteFriendShipBlackAfterCallback()) {
                DeleteFriendBlackAfterCallbackDto callbackDto = new DeleteFriendBlackAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());

                callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteBlackAfter, JSONObject.toJSONString(callbackDto));
            }
            return ResponseVO.successResponse();
        }



        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO checkBlack(CheckFriendshipReq req) {

        Map<String, Integer> toIdMap
                = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));

        List<CheckFriendshipResp> result;
        if (req.getCheckType() == CheckFriendshipTypeEnum.SINGLE.getType()) {
            result = imFriendshipMapper.checkFriendshipBlack(req);
        } else {
            result = imFriendshipMapper.checkFriendshipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendshipResp::getToId,
                                CheckFriendshipResp::getStatus));
        for (String toId:
                toIdMap.keySet()) {
            if(!collect.containsKey(toId)){
                CheckFriendshipResp checkFriendShipResp = new CheckFriendshipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }

    @Transactional
    public ResponseVO doUpdateFriendship(String fromId, FriendshipDto dto, Integer appId) {

        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);

        UpdateWrapper<ImFriendshipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendshipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendshipEntity::getExtra, dto.getExtra())
                .set(ImFriendshipEntity::getRemark, dto.getRemark())
                .set(ImFriendshipEntity::getFriendSequence, seq)
                .eq(ImFriendshipEntity::getAppId, appId)
                .eq(ImFriendshipEntity::getFromId, fromId)
                .eq(ImFriendshipEntity::getToId, dto.getToId());

        int update = imFriendshipMapper.update(null, updateWrapper);
        if (update != 1){
            return ResponseVO.errorResponse();
        }

        writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);

        return ResponseVO.successResponse();
    }

    @Transactional
    public ResponseVO doAddFriendship(RequestBase requestBase, String fromId, FriendshipDto dto, Integer appId) {

        // A-B
        // Insert two records A and B into the Friends table
        // Check whether there are records. If it exists, determine the status.
        // If it has been added, it will prompt that it has been added; if it has not been added, modify the status.

        // Add A->B
        QueryWrapper<ImFriendshipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendshipEntity fromItem = imFriendshipMapper.selectOne(query);

        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);;

        if (fromItem == null) {
            // add friend
            fromItem = new ImFriendshipEntity();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());

            fromItem.setFriendSequence(seq);

            int insert = imFriendshipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendshipErrorCode.ADD_FRIEND_ERROR);
            }

            writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);

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
                update.setFriendSequence(seq);

                int result = imFriendshipMapper.update(update, query);
                if(result != 1){
                    return ResponseVO.errorResponse(FriendshipErrorCode.ADD_FRIEND_ERROR);
                }

                writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship, seq);
            }
        }

        // Add B->A
        QueryWrapper<ImFriendshipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id",appId);
        toQuery.eq("from_id",dto.getToId());
        toQuery.eq("to_id",fromId);
        ImFriendshipEntity toItem = imFriendshipMapper.selectOne(toQuery);
        if(toItem == null){
            toItem = new ImFriendshipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            BeanUtils.copyProperties(dto, toItem);
            toItem.setToId(fromId);
            toItem.setStatus(FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            // toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            toItem.setFriendSequence(seq);

            int insert = imFriendshipMapper.insert(toItem);

            writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship, seq);

        }else{
            if(FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                    toItem.getStatus()){
                ImFriendshipEntity update = new ImFriendshipEntity();
                update.setStatus(FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                update.setFriendSequence(seq);

                imFriendshipMapper.update(update,toQuery);

                writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship, seq);
            }
        }

        // Send to fromId
        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem, addFriendPack);
        addFriendPack.setSequence(seq);

        if (requestBase != null) {
            messageProducer.sendToUserClients(fromId, requestBase.getClientType(), requestBase.getImei(),
                    FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        } else {
            messageProducer.sendToUserClients(fromId, FriendshipEventCommand.FRIEND_ADD, addFriendPack, appId);
        }

        // Send to toItem
        AddFriendPack addFriendToPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addFriendPack);
        messageProducer.sendToUserClients(toItem.getFromId(), FriendshipEventCommand.FRIEND_ADD,addFriendToPack, appId);

        // After callback
        if (appConfig.isAddFriendAfterCallback()) {
            AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
            callbackDto.setFromId(fromId);
            callbackDto.setToItem(dto);

            callbackService.callback(appId, Constants.CallbackCommand.AddFriendAfter, JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO syncFriendshipList(SyncReq req) {

        if (req.getMaxLimit() > 100) {  // TODO: Use Enums
            req.setMaxLimit(100);
        }

        QueryWrapper<ImFriendshipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getOperator());
        queryWrapper.gt("friend_sequence", req.getLastSequence());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("friend_sequence");
        List<ImFriendshipEntity> list = imFriendshipMapper.selectList(queryWrapper);

        SyncResp<ImFriendshipEntity> resp = new SyncResp<>();
        if (!CollectionUtils.isEmpty(list)) {
            resp.setDataList(list);
            // set max seq
            Long friendshipMaxSeq = imFriendshipMapper.getFriendshipMaxSeq(req.getAppId(), req.getOperator());
            resp.setMaxSequence(friendshipMaxSeq);
            // set completed
            ImFriendshipEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setCompleted(maxSeqEntity.getFriendSequence() >= friendshipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }


}
