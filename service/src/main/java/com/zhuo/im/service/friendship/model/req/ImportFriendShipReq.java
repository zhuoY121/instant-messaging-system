package com.zhuo.im.service.friendship.model.req;

import com.zhuo.im.common.enums.FriendShipStatusEnum;
import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;


@Data
public class ImportFriendShipReq extends RequestBase {

    @NotBlank(message = "fromId cannot be empty")
    private String fromId;

    // Support batch import
    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto{

        private String toId;

        private String remark;

        private String addSource;

        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NOT_FRIEND.getCode();

        // block status
        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }

}
