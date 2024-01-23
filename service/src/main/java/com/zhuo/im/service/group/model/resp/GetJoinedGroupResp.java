package com.zhuo.im.service.group.model.resp;

import com.zhuo.im.service.group.dao.ImGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @description:
 **/
@Data
public class GetJoinedGroupResp {

    private Integer totalCount;

    private List<ImGroupEntity> groupList;

}
