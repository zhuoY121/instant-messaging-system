package com.zhuo.im.common.enums.command;

public enum UserEventCommand implements Command {

    // 4000
    MODIFY_USER_INFO(4000),

    USER_ONLINE_STATUS_CHANGE(4001),

    ;

    private int command;

    UserEventCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
