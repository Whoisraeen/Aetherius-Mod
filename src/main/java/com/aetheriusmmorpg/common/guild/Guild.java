package com.aetheriusmmorpg.common.guild;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Represents a guild (faction) in the game.
 * PWI-style guild with ranks, permissions, territory control, and resources.
 */
public class Guild {

    private UUID guildId;
    private String name;
    private String tag; // Guild abbreviation (3-5 letters)
    private UUID leaderId;
    private long creationTime;
    private int level;
    private long experience;
    private String announcement;
    private ResourceLocation crest; // Guild emblem/crest

    // Members
    private final Map<UUID, GuildMember> members = new HashMap<>();

    // Ranks
    private final Map<String, GuildRank> ranks = new HashMap<>();

    // Guild Bank
    private int guildGold;
    private final Map<ResourceLocation, Integer> guildResources = new HashMap<>();

    // Territory
    private final Set<ResourceLocation> controlledTerritories = new HashSet<>();

    // Settings
    private int maxMembers = 50; // Increases with guild level
    private boolean recruitmentOpen = false;
    private int requiredLevel = 1; // Minimum player level to join

    public Guild(UUID guildId, String name, String tag, UUID leaderId) {
        this.guildId = guildId;
        this.name = name;
        this.tag = tag;
        this.leaderId = leaderId;
        this.creationTime = System.currentTimeMillis();
        this.level = 1;
        this.experience = 0;
        this.guildGold = 0;
        this.announcement = "Welcome to " + name + "!";

        initializeDefaultRanks();
    }

    private void initializeDefaultRanks() {
        // PWI-style ranks: Leader, Marshal, Executioner, Commander, Officer, Member
        ranks.put("Leader", new GuildRank("Leader", 6, createLeaderPermissions()));
        ranks.put("Marshal", new GuildRank("Marshal", 5, createMarshalPermissions()));
        ranks.put("Executioner", new GuildRank("Executioner", 4, createExecutionerPermissions()));
        ranks.put("Commander", new GuildRank("Commander", 3, createCommanderPermissions()));
        ranks.put("Officer", new GuildRank("Officer", 2, createOfficerPermissions()));
        ranks.put("Member", new GuildRank("Member", 1, createMemberPermissions()));
    }

    private EnumSet<GuildPermission> createLeaderPermissions() {
        return EnumSet.allOf(GuildPermission.class);
    }

    private EnumSet<GuildPermission> createMarshalPermissions() {
        return EnumSet.of(
            GuildPermission.INVITE_MEMBERS,
            GuildPermission.KICK_MEMBERS,
            GuildPermission.PROMOTE_MEMBERS,
            GuildPermission.MANAGE_RANKS,
            GuildPermission.EDIT_ANNOUNCEMENT,
            GuildPermission.MANAGE_BANK,
            GuildPermission.MANAGE_TERRITORY
        );
    }

    private EnumSet<GuildPermission> createExecutionerPermissions() {
        return EnumSet.of(
            GuildPermission.INVITE_MEMBERS,
            GuildPermission.KICK_MEMBERS,
            GuildPermission.EDIT_ANNOUNCEMENT,
            GuildPermission.WITHDRAW_BANK
        );
    }

    private EnumSet<GuildPermission> createCommanderPermissions() {
        return EnumSet.of(
            GuildPermission.INVITE_MEMBERS,
            GuildPermission.EDIT_ANNOUNCEMENT
        );
    }

    private EnumSet<GuildPermission> createOfficerPermissions() {
        return EnumSet.of(
            GuildPermission.INVITE_MEMBERS,
            GuildPermission.GUILD_CHAT
        );
    }

    private EnumSet<GuildPermission> createMemberPermissions() {
        return EnumSet.of(
            GuildPermission.GUILD_CHAT,
            GuildPermission.VIEW_ROSTER
        );
    }

    // Member Management
    public void addMember(UUID playerId, String playerName) {
        GuildMember member = new GuildMember(playerId, playerName, "Member");
        members.put(playerId, member);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public GuildMember getMember(UUID playerId) {
        return members.get(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }

    public boolean hasPermission(UUID playerId, GuildPermission permission) {
        GuildMember member = members.get(playerId);
        if (member == null) return false;

        GuildRank rank = ranks.get(member.getRank());
        if (rank == null) return false;

        return rank.hasPermission(permission);
    }

    public void setMemberRank(UUID playerId, String rankName) {
        GuildMember member = members.get(playerId);
        if (member != null && ranks.containsKey(rankName)) {
            member.setRank(rankName);
        }
    }

    // Guild Bank
    public void depositGold(int amount) {
        this.guildGold += amount;
    }

    public boolean withdrawGold(int amount) {
        if (guildGold >= amount) {
            guildGold -= amount;
            return true;
        }
        return false;
    }

    public void depositResource(ResourceLocation resourceId, int amount) {
        guildResources.put(resourceId, guildResources.getOrDefault(resourceId, 0) + amount);
    }

    public boolean withdrawResource(ResourceLocation resourceId, int amount) {
        int current = guildResources.getOrDefault(resourceId, 0);
        if (current >= amount) {
            guildResources.put(resourceId, current - amount);
            return true;
        }
        return false;
    }

    // Experience & Leveling
    public void addExperience(long exp) {
        this.experience += exp;
        checkLevelUp();
    }

    private void checkLevelUp() {
        long requiredExp = getRequiredExperienceForNextLevel();
        while (experience >= requiredExp && level < 10) {
            experience -= requiredExp;
            level++;
            onLevelUp();
            requiredExp = getRequiredExperienceForNextLevel();
        }
    }

    private void onLevelUp() {
        // Increase max members
        maxMembers = 50 + (level * 10);
    }

    public long getRequiredExperienceForNextLevel() {
        // Exponential growth formula
        return (long) (10000 * Math.pow(1.5, level - 1));
    }

    // Territory Management
    public void addTerritory(ResourceLocation territoryId) {
        controlledTerritories.add(territoryId);
    }

    public void removeTerritory(ResourceLocation territoryId) {
        controlledTerritories.remove(territoryId);
    }

    public boolean controlsTerritory(ResourceLocation territoryId) {
        return controlledTerritories.contains(territoryId);
    }

    // NBT Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("GuildId", guildId);
        tag.putString("Name", name);
        tag.putString("Tag", tag);
        tag.putUUID("LeaderId", leaderId);
        tag.putLong("CreationTime", creationTime);
        tag.putInt("Level", level);
        tag.putLong("Experience", experience);
        tag.putString("Announcement", announcement);
        tag.putInt("GuildGold", guildGold);
        tag.putInt("MaxMembers", maxMembers);
        tag.putBoolean("RecruitmentOpen", recruitmentOpen);
        tag.putInt("RequiredLevel", requiredLevel);

        // Members
        ListTag membersTag = new ListTag();
        for (GuildMember member : members.values()) {
            membersTag.add(member.serializeNBT());
        }
        tag.put("Members", membersTag);

        // Ranks
        ListTag ranksTag = new ListTag();
        for (GuildRank rank : ranks.values()) {
            ranksTag.add(rank.serializeNBT());
        }
        tag.put("Ranks", ranksTag);

        // Resources
        CompoundTag resourcesTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Integer> entry : guildResources.entrySet()) {
            resourcesTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("Resources", resourcesTag);

        // Territories
        ListTag territoriesTag = new ListTag();
        for (ResourceLocation territory : controlledTerritories) {
            CompoundTag territoryTag = new CompoundTag();
            territoryTag.putString("TerritoryId", territory.toString());
            territoriesTag.add(territoryTag);
        }
        tag.put("Territories", territoriesTag);

        return tag;
    }

    public static Guild deserializeNBT(CompoundTag tag) {
        UUID guildId = tag.getUUID("GuildId");
        String name = tag.getString("Name");
        String guildTag = tag.getString("Tag");
        UUID leaderId = tag.getUUID("LeaderId");

        Guild guild = new Guild(guildId, name, guildTag, leaderId);
        guild.creationTime = tag.getLong("CreationTime");
        guild.level = tag.getInt("Level");
        guild.experience = tag.getLong("Experience");
        guild.announcement = tag.getString("Announcement");
        guild.guildGold = tag.getInt("GuildGold");
        guild.maxMembers = tag.getInt("MaxMembers");
        guild.recruitmentOpen = tag.getBoolean("RecruitmentOpen");
        guild.requiredLevel = tag.getInt("RequiredLevel");

        // Members
        ListTag membersTag = tag.getList("Members", 10);
        for (int i = 0; i < membersTag.size(); i++) {
            GuildMember member = GuildMember.deserializeNBT(membersTag.getCompound(i));
            guild.members.put(member.getPlayerId(), member);
        }

        // Ranks
        ListTag ranksTag = tag.getList("Ranks", 10);
        for (int i = 0; i < ranksTag.size(); i++) {
            GuildRank rank = GuildRank.deserializeNBT(ranksTag.getCompound(i));
            guild.ranks.put(rank.getName(), rank);
        }

        // Resources
        CompoundTag resourcesTag = tag.getCompound("Resources");
        for (String key : resourcesTag.getAllKeys()) {
            guild.guildResources.put(new ResourceLocation(key), resourcesTag.getInt(key));
        }

        // Territories
        ListTag territoriesTag = tag.getList("Territories", 10);
        for (int i = 0; i < territoriesTag.size(); i++) {
            CompoundTag territoryTag = territoriesTag.getCompound(i);
            guild.controlledTerritories.add(new ResourceLocation(territoryTag.getString("TerritoryId")));
        }

        return guild;
    }

    // Getters & Setters
    public UUID getGuildId() { return guildId; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public UUID getLeaderId() { return leaderId; }
    public int getLevel() { return level; }
    public long getExperience() { return experience; }
    public String getAnnouncement() { return announcement; }
    public void setAnnouncement(String announcement) { this.announcement = announcement; }
    public int getGuildGold() { return guildGold; }
    public Map<UUID, GuildMember> getMembers() { return new HashMap<>(members); }
    public Map<String, GuildRank> getRanks() { return new HashMap<>(ranks); }
    public Set<ResourceLocation> getControlledTerritories() { return new HashSet<>(controlledTerritories); }
    public int getMaxMembers() { return maxMembers; }
    public boolean isRecruitmentOpen() { return recruitmentOpen; }
    public void setRecruitmentOpen(boolean open) { this.recruitmentOpen = open; }
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int level) { this.requiredLevel = level; }
}
