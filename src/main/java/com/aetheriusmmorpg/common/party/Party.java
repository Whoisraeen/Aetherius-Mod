package com.aetheriusmmorpg.common.party;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Represents a party of players (max 10 members like PWI).
 * Handles member management, loot distribution, and XP sharing.
 */
public class Party {

    private UUID partyId;
    private UUID leaderId;
    private final List<UUID> members = new ArrayList<>();
    private LootMode lootMode = LootMode.FREE_FOR_ALL;
    private boolean shareExperience = true;
    private int experienceRange = 50; // blocks
    private long creationTime;

    public Party(UUID leaderId) {
        this.partyId = UUID.randomUUID();
        this.leaderId = leaderId;
        this.members.add(leaderId);
        this.creationTime = System.currentTimeMillis();
    }

    public Party(UUID partyId, UUID leaderId) {
        this.partyId = partyId;
        this.leaderId = leaderId;
        this.members.add(leaderId);
        this.creationTime = System.currentTimeMillis();
    }

    public enum LootMode {
        FREE_FOR_ALL,      // Anyone can loot
        ROUND_ROBIN,       // Takes turns
        LEADER_ONLY,       // Leader decides who gets what
        RANDOM,            // Random member gets loot
        NEED_BEFORE_GREED  // Roll system for quality items
    }

    /**
     * Add a member to the party.
     */
    public boolean addMember(UUID playerId) {
        if (members.size() >= 10) {
            return false; // Max party size
        }
        if (members.contains(playerId)) {
            return false; // Already in party
        }
        members.add(playerId);
        return true;
    }

    /**
     * Remove a member from the party.
     */
    public boolean removeMember(UUID playerId) {
        if (playerId.equals(leaderId)) {
            // If leader leaves, disband or transfer leadership
            if (members.size() > 1) {
                transferLeadership(members.get(1));
            }
        }
        return members.remove(playerId);
    }

    /**
     * Transfer party leadership to another member.
     */
    public boolean transferLeadership(UUID newLeaderId) {
        if (!members.contains(newLeaderId)) {
            return false;
        }
        this.leaderId = newLeaderId;
        return true;
    }

    /**
     * Check if a player is the party leader.
     */
    public boolean isLeader(UUID playerId) {
        return leaderId.equals(playerId);
    }

    /**
     * Check if a player is in the party.
     */
    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    /**
     * Get all members except the specified player.
     */
    public List<UUID> getMembersExcept(UUID playerId) {
        List<UUID> others = new ArrayList<>(members);
        others.remove(playerId);
        return others;
    }

    /**
     * Check if party is full.
     */
    public boolean isFull() {
        return members.size() >= 10;
    }

    /**
     * Check if two players are in XP sharing range.
     */
    public boolean isInRange(Player player1, Player player2) {
        if (player1.level() != player2.level()) {
            return false; // Different dimensions
        }
        double distance = player1.distanceTo(player2);
        return distance <= experienceRange;
    }

    /**
     * Get all members in XP sharing range of a player.
     */
    public List<UUID> getMembersInRange(ServerPlayer player, List<ServerPlayer> allPlayers) {
        List<UUID> inRange = new ArrayList<>();
        for (ServerPlayer other : allPlayers) {
            if (members.contains(other.getUUID()) && isInRange(player, other)) {
                inRange.add(other.getUUID());
            }
        }
        return inRange;
    }

    // Getters and Setters
    public UUID getPartyId() {
        return partyId;
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public int getMemberCount() {
        return members.size();
    }

    public LootMode getLootMode() {
        return lootMode;
    }

    public void setLootMode(LootMode lootMode) {
        this.lootMode = lootMode;
    }

    public boolean isShareExperience() {
        return shareExperience;
    }

    public void setShareExperience(boolean shareExperience) {
        this.shareExperience = shareExperience;
    }

    public int getExperienceRange() {
        return experienceRange;
    }

    public void setExperienceRange(int experienceRange) {
        this.experienceRange = experienceRange;
    }

    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Serialize party data to NBT.
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PartyId", partyId);
        tag.putUUID("LeaderId", leaderId);
        tag.putString("LootMode", lootMode.name());
        tag.putBoolean("ShareExperience", shareExperience);
        tag.putInt("ExperienceRange", experienceRange);
        tag.putLong("CreationTime", creationTime);

        ListTag membersTag = new ListTag();
        for (UUID member : members) {
            CompoundTag memberTag = new CompoundTag();
            memberTag.putUUID("UUID", member);
            membersTag.add(memberTag);
        }
        tag.put("Members", membersTag);

        return tag;
    }

    /**
     * Deserialize party data from NBT.
     */
    public static Party deserializeNBT(CompoundTag tag) {
        UUID partyId = tag.getUUID("PartyId");
        UUID leaderId = tag.getUUID("LeaderId");

        Party party = new Party(partyId, leaderId);
        party.members.clear(); // Remove leader added in constructor

        if (tag.contains("LootMode")) {
            try {
                party.lootMode = LootMode.valueOf(tag.getString("LootMode"));
            } catch (IllegalArgumentException ignored) {}
        }

        party.shareExperience = tag.getBoolean("ShareExperience");
        party.experienceRange = tag.getInt("ExperienceRange");
        party.creationTime = tag.getLong("CreationTime");

        if (tag.contains("Members")) {
            ListTag membersTag = tag.getList("Members", Tag.TAG_COMPOUND);
            for (int i = 0; i < membersTag.size(); i++) {
                CompoundTag memberTag = membersTag.getCompound(i);
                party.members.add(memberTag.getUUID("UUID"));
            }
        }

        return party;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return Objects.equals(partyId, party.partyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partyId);
    }
}
