package com.aetheriusmmorpg.common.dungeon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Represents an active dungeon instance for a party.
 */
public class DungeonInstance {

    private final UUID instanceId;
    private final ResourceLocation dungeonId;
    private final UUID partyId;
    private final List<UUID> participants;
    private ResourceKey<Level> dimensionKey;

    private long startTime;
    private long expirationTime;
    private DungeonStatus status;

    private final Set<String> defeatedBosses;
    private int currentPhase;

    public DungeonInstance(UUID instanceId, ResourceLocation dungeonId, UUID partyId, List<UUID> participants, long duration) {
        this.instanceId = instanceId;
        this.dungeonId = dungeonId;
        this.partyId = partyId;
        this.participants = new ArrayList<>(participants);
        this.startTime = System.currentTimeMillis();
        this.expirationTime = startTime + duration;
        this.status = DungeonStatus.ACTIVE;
        this.defeatedBosses = new HashSet<>();
        this.currentPhase = 1;
    }

    public enum DungeonStatus {
        ACTIVE,
        COMPLETED,
        FAILED,
        EXPIRED
    }

    /**
     * Check if instance has expired.
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    /**
     * Get remaining time in milliseconds.
     */
    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }

    /**
     * Get remaining time in seconds.
     */
    public int getRemainingSeconds() {
        return (int) (getRemainingTime() / 1000);
    }

    /**
     * Mark a boss as defeated.
     */
    public void defeatBoss(String bossId) {
        defeatedBosses.add(bossId);
    }

    /**
     * Check if a boss has been defeated.
     */
    public boolean isBossDefeated(String bossId) {
        return defeatedBosses.contains(bossId);
    }

    /**
     * Check if all bosses are defeated.
     */
    public boolean areAllBossesDefeated(Dungeon dungeon) {
        for (Dungeon.BossEncounter boss : dungeon.bosses()) {
            if (!defeatedBosses.contains(boss.bossId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a player is a participant.
     */
    public boolean isParticipant(UUID playerId) {
        return participants.contains(playerId);
    }

    /**
     * Complete the dungeon.
     */
    public void complete() {
        this.status = DungeonStatus.COMPLETED;
    }

    /**
     * Fail the dungeon.
     */
    public void fail() {
        this.status = DungeonStatus.FAILED;
    }

    /**
     * Mark as expired.
     */
    public void expire() {
        this.status = DungeonStatus.EXPIRED;
    }

    // Getters
    public UUID getInstanceId() {
        return instanceId;
    }

    public ResourceLocation getDungeonId() {
        return dungeonId;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public List<UUID> getParticipants() {
        return new ArrayList<>(participants);
    }

    public long getStartTime() {
        return startTime;
    }

    public DungeonStatus getStatus() {
        return status;
    }

    public Set<String> getDefeatedBosses() {
        return new HashSet<>(defeatedBosses);
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(int phase) {
        this.currentPhase = phase;
    }

    public ResourceKey<Level> getDimensionKey() {
        return dimensionKey;
    }

    public void setDimensionKey(ResourceKey<Level> dimensionKey) {
        this.dimensionKey = dimensionKey;
    }

    /**
     * Serialize to NBT.
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("InstanceId", instanceId);
        tag.putString("DungeonId", dungeonId.toString());
        tag.putUUID("PartyId", partyId);

        ListTag participantsTag = new ListTag();
        for (UUID participant : participants) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("UUID", participant);
            participantsTag.add(pTag);
        }
        tag.put("Participants", participantsTag);

        tag.putLong("StartTime", startTime);
        tag.putLong("ExpirationTime", expirationTime);
        tag.putString("Status", status.name());
        tag.putInt("CurrentPhase", currentPhase);

        ListTag bossesTag = new ListTag();
        for (String bossId : defeatedBosses) {
            CompoundTag bTag = new CompoundTag();
            bTag.putString("BossId", bossId);
            bossesTag.add(bTag);
        }
        tag.put("DefeatedBosses", bossesTag);

        if (dimensionKey != null) {
            tag.putString("DimensionKey", dimensionKey.location().toString());
        }

        return tag;
    }

    /**
     * Deserialize from NBT.
     */
    public static DungeonInstance deserializeNBT(CompoundTag tag) {
        UUID instanceId = tag.getUUID("InstanceId");
        ResourceLocation dungeonId = new ResourceLocation(tag.getString("DungeonId"));
        UUID partyId = tag.getUUID("PartyId");

        List<UUID> participants = new ArrayList<>();
        if (tag.contains("Participants")) {
            ListTag participantsTag = tag.getList("Participants", Tag.TAG_COMPOUND);
            for (int i = 0; i < participantsTag.size(); i++) {
                participants.add(participantsTag.getCompound(i).getUUID("UUID"));
            }
        }

        long startTime = tag.getLong("StartTime");
        long expirationTime = tag.getLong("ExpirationTime");
        long duration = expirationTime - startTime;

        DungeonInstance instance = new DungeonInstance(instanceId, dungeonId, partyId, participants, duration);
        instance.startTime = startTime;
        instance.expirationTime = expirationTime;
        instance.currentPhase = tag.getInt("CurrentPhase");

        if (tag.contains("Status")) {
            try {
                instance.status = DungeonStatus.valueOf(tag.getString("Status"));
            } catch (IllegalArgumentException ignored) {}
        }

        if (tag.contains("DefeatedBosses")) {
            ListTag bossesTag = tag.getList("DefeatedBosses", Tag.TAG_COMPOUND);
            for (int i = 0; i < bossesTag.size(); i++) {
                instance.defeatedBosses.add(bossesTag.getCompound(i).getString("BossId"));
            }
        }

        if (tag.contains("DimensionKey")) {
            // Dimension key will be resolved when loaded
        }

        return instance;
    }
}
