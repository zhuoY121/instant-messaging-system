package com.zhuo.im.service.friendship.model.callback;

import com.zhuo.im.service.friendship.model.req.FriendshipDto;
import lombok.Data;

/**
 * @description:
 * @version: 1.0
 */
@Data
public class UpdateFriendAfterCallbackDto {

    private String fromId;

    private FriendshipDto toItem;
}
