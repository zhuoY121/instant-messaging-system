package com.zhuo.im.service.friendship.model.resp;

import lombok.Data;


@Data
public class CheckFriendshipResp {

    private String fromId;

    private String toId;

    // Verification status, there are different statuses according to two-way verification and one-way verification
    // One-way verification: 1=fromId added toId, not sure whether toId added fromId => CheckResult_single_Type_AWithB;
    //  0=fromId has not added toId, and it is not sure whether toId has been added fromId => CheckResult_single_Type_NoRelation
    // Two-way verification: 1=fromId added toId, toId also added from => CheckResult_Type_BothWay
    //  2=fromId added toId, toId did not add fromId => CheckResult_Both_Type_AWithB
    //  3=fromId does not add toId, toId adds fromId => CheckResult_Both_Type_BWithA
    //  4=Neither party is added => CheckResult_Both_Type_NoRelation

    // One-way verification blacklist: 1=fromId has not blocked toId, not sure whether toId has been blocked fromId => CheckResult_singe_Type_AWithB;
    //  0=fromId blacklist toId, not sure toId is blacklist fromId => CheckResult_singe_Type_NoRelation
    // Two-way verification blacklist: 1=fromId does not blacklist toId, and toId does not blacklist fromId => CheckResult_Type_BothWay
    //  2=fromId does not block toId, toId blocks fromId => CheckResult_Both_Type_AWithB
    //  3=fromId has blocked toId, but toId has not blocked fromId => CheckResult_Both_Type_BWithA
    //  4=Both parties are blocked => CheckResult_Both_Type_NoRelation
    private Integer status;

}
