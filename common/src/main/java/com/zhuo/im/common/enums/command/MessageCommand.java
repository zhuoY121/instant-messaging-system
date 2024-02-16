package com.zhuo.im.common.enums.command;

public enum MessageCommand implements Command {

    // Single chat message 1103
    MSG_P2P(0x44F),

    // Single chat message ACK 1046
    MSG_ACK(0x416),

    // Message received ack 1107
    MSG_RECEIVE_ACK(1107),

    // Send message read 1106
    MSG_READ(0x452),

    // Notify the synchronization terminal that the message has been read 1053
    MSG_READ_NOTIFICATION(0x41D),

    // Message read receipt, given to the original message sender 1054
    MSG_READ_RECEIPT(0x41E),

    // Message withdrawn 1050
    MSG_RECALL(0x41A),

    // Message withdrawal notification 1052
    MSG_RECALL_NOTIFICATION(0x41C),

    // Message withdrawal ACK 1051
    MSG_RECALL_ACK(0x41B),

    ;

    private int command;

    MessageCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
