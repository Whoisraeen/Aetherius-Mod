package com.aetheriusmmorpg.client;

import com.aetheriusmmorpg.network.packet.guild.S2CGuildDataPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side cache of guild data received from server.
 */
public class ClientGuildData {

    private static boolean hasGuild = false;
    private static UUID guildId = UUID.randomUUID();
    private static String guildName = "";
    private static String guildTag = "";
    private static UUID leaderId = UUID.randomUUID();
    private static int level = 1;
    private static long experience = 0;
    private static long expForNextLevel = 0;
    private static String announcement = "";
    private static int guildGold = 0;
    private static int maxMembers = 0;
    private static Map<UUID, S2CGuildDataPacket.MemberData> members = new HashMap<>();

    public static void setGuildData(
        UUID id,
        String name,
        String tag,
        UUID leader,
        int lvl,
        long exp,
        long expNext,
        String announce,
        int gold,
        int maxMem,
        Map<UUID, S2CGuildDataPacket.MemberData> mem
    ) {
        hasGuild = true;
        guildId = id;
        guildName = name;
        guildTag = tag;
        leaderId = leader;
        level = lvl;
        experience = exp;
        expForNextLevel = expNext;
        announcement = announce;
        guildGold = gold;
        maxMembers = maxMem;
        members = new HashMap<>(mem);
    }

    public static void clearGuildData() {
        hasGuild = false;
        guildId = UUID.randomUUID();
        guildName = "";
        guildTag = "";
        leaderId = UUID.randomUUID();
        level = 1;
        experience = 0;
        expForNextLevel = 0;
        announcement = "";
        guildGold = 0;
        maxMembers = 0;
        members.clear();
    }

    // Getters
    public static boolean hasGuild() { return hasGuild; }
    public static UUID getGuildId() { return guildId; }
    public static String getGuildName() { return guildName; }
    public static String getGuildTag() { return guildTag; }
    public static UUID getLeaderId() { return leaderId; }
    public static int getLevel() { return level; }
    public static long getExperience() { return experience; }
    public static long getExpForNextLevel() { return expForNextLevel; }
    public static String getAnnouncement() { return announcement; }
    public static int getGuildGold() { return guildGold; }
    public static int getMaxMembers() { return maxMembers; }
    public static Map<UUID, S2CGuildDataPacket.MemberData> getMembers() { return new HashMap<>(members); }
}

