package com.zhuo.im.common.enums.command;

public enum FriendshipEventCommand implements Command {

    FRIEND_ADD(3000),

    FRIEND_UPDATE(3001),

    FRIEND_DELETE(3002),

    FRIEND_REQUEST(3003),

    FRIEND_REQUEST_READ(3004),

    FRIEND_REQUEST_APPROVE(3005),

    FRIEND_BLACKLIST_ADD(3010),

    FRIEND_BLACKLIST_DELETE(3011),

    FRIEND_GROUP_ADD(3012),

    FRIEND_GROUP_DELETE(3013),

    FRIEND_GROUP_MEMBER_ADD(3014),

    FRIEND_GROUP_MEMBER_DELETE(3015),

    FRIEND_DELETE_ALL(3016),

    ;

    private int command;

    FriendshipEventCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
