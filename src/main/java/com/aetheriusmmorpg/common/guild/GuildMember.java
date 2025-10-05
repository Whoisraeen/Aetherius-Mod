package com.aetheriusmmorpg.common.guild;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Represents a member of a guild.
 */
public class GuildMember {

    private final UUID playerId;
    private String playerName;
    private String rank;
    private long joinTime;
    private long contribution; // Guild contribution points
    private long lastOnline;

    public GuildMember(UUID playerId, String playerName, String rank) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.rank = rank;
        this.joinTime = System.currentTimeMillis();
        this.contribution = 0;
        this.lastOnline = System.currentTimeMillis();
    }

    public void addContribution(long amount) {
        this.contribution += amount;
    }

    public void updateLastOnline() {
        this.lastOnline = System.currentTimeMillis();
    }

    // NBT Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerId", playerId);
        tag.putString("PlayerName", playerName);
        tag.putString("Rank", rank);
        tag.putLong("JoinTime", joinTime);
        tag.putLong("Contribution", contribution);
        tag.putLong("LastOnline", lastOnline);
        return tag;
    }

    public static GuildMember deserializeNBT(CompoundTag tag) {
        UUID playerId = tag.getUUID("PlayerId");
        String playerName = tag.getString("PlayerName");
        String rank = tag.getString("Rank");

        GuildMember member = new GuildMember(playerId, playerName, rank);
        member.joinTime = tag.getLong("JoinTime");
        member.contribution = tag.getLong("Contribution");
        member.lastOnline = tag.getLong("LastOnline");
        return member;
    }

    // Getters & Setters
    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String name) { this.playerName = name; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public long getJoinTime() { return joinTime; }
    public long getContribution() { return contribution; }
    public long getLastOnline() { return lastOnline; }
}
