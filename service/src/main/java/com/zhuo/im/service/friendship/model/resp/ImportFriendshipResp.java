package com.zhuo.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;


@Data
public class ImportFriendshipResp {

    private List<String> successId;

    private List<String> errorId;
}
