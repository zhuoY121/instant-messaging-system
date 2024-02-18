package com.zhuo.im.service.message.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.config.AppConfig;
import com.zhuo.im.common.enums.*;
import com.zhuo.im.service.friendship.dao.ImFriendshipEntity;
import com.zhuo.im.service.friendship.model.req.GetFriendshipReq;
import com.zhuo.im.service.friendship.service.ImFriendshipService;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.model.resp.GetRoleInGroupResp;
import com.zhuo.im.service.group.service.ImGroupMemberService;
import com.zhuo.im.service.group.service.ImGroupService;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @version: 1.0
 */
@Service
public class CheckSendMessageService {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendshipService imFriendshipService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    /**
     * Check if the sender has permission to send the message
     */
    public ResponseVO checkSenderMutedOrDisabled(String fromId, Integer appId){

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ImUserDataEntity user = singleUserInfo.getData();
        if (user.getForbiddenFlag() == UserDisabledFlagEnum.DISABLED.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.SENDER_DISABLED);
        } else if (user.getSilentFlag() == UserMutedFlagEnum.MUTED.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.SENDER_MUTED);
        }

        return ResponseVO.successResponse();
    }


    public ResponseVO checkFriendship(String fromId, String toId, Integer appId){

        if (appConfig.isSendMessageCheckFriendship()) {

            GetFriendshipReq fromReq = new GetFriendshipReq();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendshipEntity> fromFriendship = imFriendshipService.getFriendship(fromReq);
            if (!fromFriendship.isOk()) {
                return fromFriendship;
            }

            GetFriendshipReq toReq = new GetFriendshipReq();
            toReq.setFromId(toId);
            toReq.setToId(fromId);
            toReq.setAppId(appId);
            ResponseVO<ImFriendshipEntity> toFriendship = imFriendshipService.getFriendship(toReq);
            if (!toFriendship.isOk()) {
                return toFriendship;
            }

            if (FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != fromFriendship.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_IS_DELETED);
            }

            if (FriendshipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toFriendship.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_IS_DELETED);
            }

            if (appConfig.isSendMessageCheckBlacklist()) {
                if (fromFriendship.getData().getBlack() != null &&
                        FriendshipStatusEnum.BLACK_STATUS_NORMAL.getCode() != fromFriendship.getData().getBlack()) {
                    return ResponseVO.errorResponse(FriendshipErrorCode.FRIEND_IN_BLACKLIST);
                }

                if (toFriendship.getData().getBlack() != null &&
                        FriendshipStatusEnum.BLACK_STATUS_NORMAL.getCode() != toFriendship.getData().getBlack()) {
                    return ResponseVO.errorResponse(FriendshipErrorCode.TARGET_BLOCK_YOU);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO checkGroupMessage(String fromId,String groupId,Integer appId){

        ResponseVO responseVO = checkSenderMutedOrDisabled(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        // Check group
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(groupId, appId);
        if (!group.isOk()) {
            return group;
        }

        // Check whether the member is in the group
        ResponseVO<GetRoleInGroupResp> roleInGroup = imGroupMemberService.getRoleInGroup(groupId, fromId, appId);
        if (!roleInGroup.isOk()) {
            return roleInGroup;
        }
        GetRoleInGroupResp data = roleInGroup.getData();

        // Determine whether the group is muted
        // If muted, only group managers and group owners can speak.
        ImGroupEntity groupData = group.getData();
        if (groupData.getMute() == GroupMuteTypeEnum.MUTED.getCode() &&
                !(data.getRole() == GroupMemberRoleEnum.MANAGER.getCode() ||
                        data.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MUTED);
        }

        if (data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_MUTED);
        }

        return ResponseVO.successResponse();
    }

}
