package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.enums.FriendshipStatusEnum;
import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;


@Data
public class ImportFriendshipReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

    // Support batch import
    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto{

        private String toId;

        private String remark;

        private String addSource;

        private Integer status = FriendshipStatusEnum.FRIEND_STATUS_NOT_FRIEND.getCode();

        // block status
        private Integer black = FriendshipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }

}
