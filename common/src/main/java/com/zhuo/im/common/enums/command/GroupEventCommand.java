package com.zhuo.im.common.enums.command;

//2
public enum GroupEventCommand implements Command {


    /**
     * Push notification of application to join the group
     */
    JOIN_GROUP(2000),

    /**
     * Push add group members notification to all administrators and myself
     */
    ADD_MEMBER(2001),

    /**
     * Push creation group notification to everyone
     */
    CREATE_GROUP(2002),

    /**
     * Push update group notification to everyone
     */
    UPDATE_GROUP(2003),

    /**
     * Push exit group notification to administrators and operators
     */
    EXIT_GROUP(2004),

    /**
     * Push modify group member notification to the administrator and the operated person
     */
    UPDATE_MEMBER(2005),

    /**
     * Push group member deletion notification to all group members and people to be removed
     */
    DELETE_MEMBER(2006),

    /**
     * Push group disbandment notification to everyone
     */
    DELETE_GROUP(2007),

    /**
     * Push transfer group owner notification to everyone
     */
    TRANSFER_GROUP(2008),

    /**
     * Mute group, notify everyone
     */
    MUTE_GROUP(2009),


    /**
     * Mute group member, notifying the administrator and the person being manipulated
     */
    MUTE_GROUP_MEMBER(2010),

    ;

    private Integer command;

    GroupEventCommand(int command) {
        this.command = command;
    }


    public int getCommand() {
        return command;
    }
}
