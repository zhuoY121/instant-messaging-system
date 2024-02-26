package com.zhuo.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhuo.im.codec.pack.friendship.AddFriendGroupPack;
import com.zhuo.im.codec.pack.friendship.DeleteFriendGroupPack;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.constant.Constants;
import com.zhuo.im.common.enums.DelFlagEnum;
import com.zhuo.im.common.enums.FriendshipErrorCode;
import com.zhuo.im.common.enums.FriendshipStatusEnum;
import com.zhuo.im.common.enums.command.FriendshipEventCommand;
import com.zhuo.im.common.model.ClientInfo;
import com.zhuo.im.service.friendship.dao.ImFriendshipEntity;
import com.zhuo.im.service.friendship.dao.ImFriendshipGroupEntity;
import com.zhuo.im.service.friendship.dao.mapper.ImFriendshipGroupMapper;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupMemberReq;
import com.zhuo.im.service.friendship.model.req.AddFriendshipGroupReq;
import com.zhuo.im.service.friendship.model.req.DeleteFriendshipGroupReq;
import com.zhuo.im.service.friendship.service.ImFriendshipGroupMemberService;
import com.zhuo.im.service.friendship.service.ImFriendshipGroupService;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.seq.RedisSeq;
import com.zhuo.im.service.user.service.ImUserService;
import com.zhuo.im.service.utils.MessageProducer;
import com.zhuo.im.service.utils.WriteUserSeq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImFriendshipGroupServiceImpl implements ImFriendshipGroupService {

    @Autowired
    ImFriendshipGroupMapper imFriendshipGroupMapper;

    @Autowired
    ImFriendshipGroupMemberService imFriendshipGroupMemberService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;


    @Override
    @Transactional
    public ResponseVO addGroup(AddFriendshipGroupReq req) {

        QueryWrapper<ImFriendshipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", req.getGroupName());
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
//        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImFriendshipGroupEntity entity = imFriendshipGroupMapper.selectOne(query);

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup);

        if (entity == null) {
            // write to DB
            ImFriendshipGroupEntity insert = new ImFriendshipGroupEntity();
            insert.setAppId(req.getAppId());
            insert.setCreateTime(System.currentTimeMillis());
            insert.setDelFlag(DelFlagEnum.NORMAL.getCode());
            insert.setGroupName(req.getGroupName());
            insert.setFromId(req.getFromId());
            insert.setSequence(seq);
            int insert1 = imFriendshipGroupMapper.insert(insert);

            if (insert1 != 1) {
                return ResponseVO.errorResponse(FriendshipErrorCode.FRIENDSHIP_GROUP_CREATE_ERROR);
            } else {
                if (CollectionUtil.isNotEmpty(req.getToIds())) {
                    AddFriendshipGroupMemberReq addFriendshipGroupMemberReq = new AddFriendshipGroupMemberReq();
                    addFriendshipGroupMemberReq.setFromId(req.getFromId());
                    addFriendshipGroupMemberReq.setGroupName(req.getGroupName());
                    addFriendshipGroupMemberReq.setToIds(req.getToIds());
                    addFriendshipGroupMemberReq.setAppId(req.getAppId());
                    imFriendshipGroupMemberService.addGroupMember(addFriendshipGroupMemberReq);
                    return ResponseVO.successResponse();
                }
            }

        } else {
            if (entity.getDelFlag() == DelFlagEnum.NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendshipErrorCode.FRIENDSHIP_GROUP_EXIST);
            } else {
                ImFriendshipGroupEntity update = new ImFriendshipGroupEntity();
                update.setDelFlag(DelFlagEnum.NORMAL.getCode());
                update.setUpdateTime(System.currentTimeMillis());
                int update1 = imFriendshipGroupMapper.update(update, query);
                if (update1 != 1) {
                    return ResponseVO.errorResponse(FriendshipErrorCode.FRIENDSHIP_GROUP_UPDATE_ERROR);
                } else {
                    if (CollectionUtil.isNotEmpty(req.getToIds())) {
                        AddFriendshipGroupMemberReq addFriendshipGroupMemberReq = new AddFriendshipGroupMemberReq();
                        addFriendshipGroupMemberReq.setFromId(req.getFromId());
                        addFriendshipGroupMemberReq.setGroupName(req.getGroupName());
                        addFriendshipGroupMemberReq.setToIds(req.getToIds());
                        addFriendshipGroupMemberReq.setAppId(req.getAppId());
                        imFriendshipGroupMemberService.addGroupMember(addFriendshipGroupMemberReq);
                        return ResponseVO.successResponse();
                    }
                }
            }
        }

        writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);

        // Send tcp notification
        AddFriendGroupPack addFriendGropPack = new AddFriendGroupPack();
        addFriendGropPack.setFromId(req.getFromId());
        addFriendGropPack.setGroupName(req.getGroupName());
        addFriendGropPack.setSequence(seq);
        messageProducer.sendToUserClientsExceptOne(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_ADD,
                addFriendGropPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO deleteGroup(DeleteFriendshipGroupReq req) {

        for (String groupName : req.getGroupNames()) {
            QueryWrapper<ImFriendshipGroupEntity> query = new QueryWrapper<>();
            query.eq("group_name", groupName);
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

            ImFriendshipGroupEntity entity = imFriendshipGroupMapper.selectOne(query);

            if (entity != null) {
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup);

                ImFriendshipGroupEntity update = new ImFriendshipGroupEntity();
                update.setGroupId(entity.getGroupId());
                update.setDelFlag(DelFlagEnum.DELETE.getCode());
                update.setSequence(seq);
                imFriendshipGroupMapper.updateById(update);
                imFriendshipGroupMemberService.clearGroupMembers(entity.getGroupId());

                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);

                // Send tcp notification
                DeleteFriendGroupPack deleteFriendGroupPack = new DeleteFriendGroupPack();
                deleteFriendGroupPack.setFromId(req.getFromId());
                deleteFriendGroupPack.setGroupName(groupName);
                deleteFriendGroupPack.setSequence(seq);
                messageProducer.sendToUserClientsExceptOne(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_DELETE,
                        deleteFriendGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String fromId, String groupName, Integer appId) {
        QueryWrapper<ImFriendshipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", groupName);
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImFriendshipGroupEntity entity = imFriendshipGroupMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendshipErrorCode.FRIENDSHIP_GROUP_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public Long updateSeq(String fromId, String groupName, Integer appId) {

        QueryWrapper<ImFriendshipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", groupName);
        query.eq("app_id", appId);
        query.eq("from_id", fromId);

        ImFriendshipGroupEntity entity = imFriendshipGroupMapper.selectOne(query);

        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.FriendshipGroup);

        ImFriendshipGroupEntity group = new ImFriendshipGroupEntity();
        group.setGroupId(entity.getGroupId());
        group.setSequence(seq);
        imFriendshipGroupMapper.updateById(group);
        return seq;
    }

}
