package com.zhuo.im.common.enums.command;

public enum SystemCommand implements Command {

    /**
     * login 9000
     */
    LOGIN(0x2328),

    // logout  9003
    LOGOUT(0x232b),

    // heart beat 9999
    PING(0x270f),
    ;

    private int command;

    SystemCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
