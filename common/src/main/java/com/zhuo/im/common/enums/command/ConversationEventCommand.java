package com.zhuo.im.common.enums.command;

public enum ConversationEventCommand implements Command {

    // delete conversation
    DELETE_CONVERSATION(5000),

    // update conversation
    UPDATE_CONVERSATION(5001),

    ;

    private int command;

    ConversationEventCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
