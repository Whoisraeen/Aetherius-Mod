package com.aetheriusmmorpg.common.guild;

/**
 * Guild permissions for rank-based access control.
 */
public enum GuildPermission {
    // Member Management
    INVITE_MEMBERS,
    KICK_MEMBERS,
    PROMOTE_MEMBERS,
    DEMOTE_MEMBERS,

    // Guild Management
    MANAGE_RANKS,
    EDIT_ANNOUNCEMENT,
    CHANGE_CREST,
    DISBAND_GUILD,

    // Communication
    GUILD_CHAT,
    SEND_ANNOUNCEMENTS,

    // Resources
    MANAGE_BANK,
    DEPOSIT_BANK,
    WITHDRAW_BANK,

    // Territory
    MANAGE_TERRITORY,
    DECLARE_WAR,
    MANAGE_ALLIANCES,

    // Information
    VIEW_ROSTER,
    VIEW_BANK,
    VIEW_LOGS,

    // Events
    START_GUILD_EVENTS,
    MANAGE_QUESTS
}
