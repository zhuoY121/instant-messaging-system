package com.zhuo.im.service.user.model.req;

import com.zhuo.im.common.model.RequestBase;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;
}
