package com.zhuo.im.service.group.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.service.group.dao.ImGroupEntity;
import com.zhuo.im.service.group.model.req.*;

/**
 * @description:
 * @version: 1.0
 */
public interface ImGroupService {

    public ResponseVO importGroup(ImportGroupReq req);

    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);
//
}
