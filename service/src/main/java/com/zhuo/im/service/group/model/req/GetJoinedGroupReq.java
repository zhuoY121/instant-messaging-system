package com.zhuo.im.service.group.model.req;

import com.zhuo.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description:
 **/
@Data
public class GetJoinedGroupReq extends RequestBase {

    @NotBlank(message = "memberId cannot be empty")
    private String memberId;

    private List<Integer> groupType;

    // The number of groups pulled in a single time. If left blank, it means all groups.
    private Integer limit;

    // which page
    private Integer offset;


}
