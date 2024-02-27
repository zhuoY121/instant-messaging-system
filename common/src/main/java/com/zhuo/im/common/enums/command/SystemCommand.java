package com.zhuo.im.common.enums.command;

public enum SystemCommand implements Command {

    /**
     * login 9000
     */
    LOGIN(0x2328),

    // logout  9003
    LOGOUT(0x232b),

    // Login ACK, 9001
    LOGIN_ACK(0x2329),

    // heart beat 9999
    PING(0x270f),

    // Offline notification used to handle multi-client login. 9002
    MULTI_CLIENT_LOGIN(0x232a),
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
