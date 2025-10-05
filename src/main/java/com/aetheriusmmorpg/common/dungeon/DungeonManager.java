package com.aetheriusmmorpg.common.dungeon;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.party.Party;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Manages all dungeons and active dungeon instances.
 * Loads dungeon definitions from datapacks and handles instance lifecycle.
 */
public class DungeonManager extends SavedData {

    private static final String DATA_NAME = "aetherius_dungeons";
    private static final Map<ResourceLocation, Dungeon> DUNGEONS = new HashMap<>();

    // Active instances
    private final Map<UUID, DungeonInstance> activeInstances = new HashMap<>();

    // Party to instance mapping
    private final Map<UUID, UUID> partyToInstance = new HashMap<>();

    // Player cooldowns (player UUID -> dungeon ID -> expiration time)
    private final Map<UUID, Map<ResourceLocation, Long>> playerCooldowns = new HashMap<>();

    public DungeonManager() {
        super();
    }

    /**
     * Get or create the dungeon manager for a server.
     */
    public static DungeonManager get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            DungeonManager::load,
            DungeonManager::new,
            DATA_NAME
        );
    }

    /**
     * JSON resource reload listener for loading dungeons from datapacks.
     */
    public static class DungeonReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public DungeonReloadListener() {
            super(GSON, "dungeons");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            DUNGEONS.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
                try {
                    Dungeon dungeon = Dungeon.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .getOrThrow(false, error -> AetheriusMod.LOGGER.error("Error parsing dungeon {}: {}", entry.getKey(), error));

                    DUNGEONS.put(dungeon.id(), dungeon);
                    AetheriusMod.LOGGER.info("Loaded dungeon: {}", dungeon.name());
                } catch (Exception e) {
                    AetheriusMod.LOGGER.error("Failed to load dungeon {}: {}", entry.getKey(), e.getMessage());
                }
            }

            AetheriusMod.LOGGER.info("Loaded {} dungeons", DUNGEONS.size());
        }
    }

    /**
     * Get a dungeon by ID.
     */
    public static Dungeon getDungeon(ResourceLocation id) {
        return DUNGEONS.get(id);
    }

    /**
     * Get all dungeons.
     */
    public static Collection<Dungeon> getAllDungeons() {
        return DUNGEONS.values();
    }

    /**
     * Get dungeons available for a player's level.
     */
    public static List<Dungeon> getAvailableDungeons(int playerLevel) {
        return DUNGEONS.values().stream()
            .filter(dungeon -> dungeon.isLevelInRange(playerLevel))
            .toList();
    }

    /**
     * Create a new dungeon instance for a party.
     */
    public DungeonInstance createInstance(Dungeon dungeon, Party party, List<ServerPlayer> players) {
        // Check if party already has an active instance
        if (partyToInstance.containsKey(party.getPartyId())) {
            return null;
        }

        // Check cooldowns
        for (ServerPlayer player : players) {
            if (isOnCooldown(player.getUUID(), dungeon.id())) {
                return null;
            }
        }

        // Create instance
        UUID instanceId = UUID.randomUUID();
        List<UUID> participantIds = players.stream().map(ServerPlayer::getUUID).toList();

        DungeonInstance instance = new DungeonInstance(
            instanceId,
            dungeon.id(),
            party.getPartyId(),
            participantIds,
            dungeon.getTimeLimitTicks() * 50L // Convert ticks to milliseconds
        );

        activeInstances.put(instanceId, instance);
        partyToInstance.put(party.getPartyId(), instanceId);

        setDirty();
        return instance;
    }

    /**
     * Get an active instance by ID.
     */
    public DungeonInstance getInstance(UUID instanceId) {
        return activeInstances.get(instanceId);
    }

    /**
     * Get the instance a party is currently in.
     */
    public DungeonInstance getPartyInstance(UUID partyId) {
        UUID instanceId = partyToInstance.get(partyId);
        return instanceId != null ? activeInstances.get(instanceId) : null;
    }

    /**
     * Complete a dungeon instance.
     */
    public void completeInstance(UUID instanceId, MinecraftServer server) {
        DungeonInstance instance = activeInstances.get(instanceId);
        if (instance == null) return;

        instance.complete();

        // Set cooldowns for all participants
        Dungeon dungeon = DUNGEONS.get(instance.getDungeonId());
        if (dungeon != null) {
            long cooldownExpiration = System.currentTimeMillis() + (dungeon.getCooldownTicks() * 50L);
            for (UUID playerId : instance.getParticipants()) {
                setCooldown(playerId, dungeon.id(), cooldownExpiration);
            }
        }

        // Clean up instance after delay
        removeInstance(instanceId);
        setDirty();
    }

    /**
     * Fail a dungeon instance.
     */
    public void failInstance(UUID instanceId) {
        DungeonInstance instance = activeInstances.get(instanceId);
        if (instance == null) return;

        instance.fail();
        removeInstance(instanceId);
        setDirty();
    }

    /**
     * Remove an instance.
     */
    private void removeInstance(UUID instanceId) {
        DungeonInstance instance = activeInstances.remove(instanceId);
        if (instance != null) {
            partyToInstance.remove(instance.getPartyId());
        }
    }

    /**
     * Check if a player is on cooldown for a dungeon.
     */
    public boolean isOnCooldown(UUID playerId, ResourceLocation dungeonId) {
        Map<ResourceLocation, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) return false;

        Long expiration = cooldowns.get(dungeonId);
        if (expiration == null) return false;

        if (System.currentTimeMillis() >= expiration) {
            cooldowns.remove(dungeonId);
            return false;
        }

        return true;
    }

    /**
     * Get remaining cooldown in seconds.
     */
    public int getRemainingCooldown(UUID playerId, ResourceLocation dungeonId) {
        Map<ResourceLocation, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) return 0;

        Long expiration = cooldowns.get(dungeonId);
        if (expiration == null) return 0;

        long remaining = expiration - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    /**
     * Set a cooldown for a player.
     */
    private void setCooldown(UUID playerId, ResourceLocation dungeonId, long expirationTime) {
        playerCooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
            .put(dungeonId, expirationTime);
    }

    /**
     * Tick all active instances to check for expiration.
     */
    public void tick(MinecraftServer server) {
        List<UUID> toRemove = new ArrayList<>();

        for (DungeonInstance instance : activeInstances.values()) {
            if (instance.hasExpired() && instance.getStatus() == DungeonInstance.DungeonStatus.ACTIVE) {
                instance.expire();
                toRemove.add(instance.getInstanceId());
            }
        }

        for (UUID instanceId : toRemove) {
            removeInstance(instanceId);
        }

        if (!toRemove.isEmpty()) {
            setDirty();
        }
    }

    // NBT Serialization
    public static DungeonManager load(CompoundTag tag) {
        DungeonManager manager = new DungeonManager();

        // Load active instances
        if (tag.contains("Instances")) {
            ListTag instancesTag = tag.getList("Instances", Tag.TAG_COMPOUND);
            for (int i = 0; i < instancesTag.size(); i++) {
                DungeonInstance instance = DungeonInstance.deserializeNBT(instancesTag.getCompound(i));
                manager.activeInstances.put(instance.getInstanceId(), instance);
                manager.partyToInstance.put(instance.getPartyId(), instance.getInstanceId());
            }
        }

        // Load cooldowns
        if (tag.contains("Cooldowns")) {
            CompoundTag cooldownsTag = tag.getCompound("Cooldowns");
            for (String playerIdStr : cooldownsTag.getAllKeys()) {
                UUID playerId = UUID.fromString(playerIdStr);
                CompoundTag playerCooldowns = cooldownsTag.getCompound(playerIdStr);

                Map<ResourceLocation, Long> dungeonCooldowns = new HashMap<>();
                for (String dungeonIdStr : playerCooldowns.getAllKeys()) {
                    ResourceLocation dungeonId = new ResourceLocation(dungeonIdStr);
                    long expiration = playerCooldowns.getLong(dungeonIdStr);
                    dungeonCooldowns.put(dungeonId, expiration);
                }

                manager.playerCooldowns.put(playerId, dungeonCooldowns);
            }
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save active instances
        ListTag instancesTag = new ListTag();
        for (DungeonInstance instance : activeInstances.values()) {
            instancesTag.add(instance.serializeNBT());
        }
        tag.put("Instances", instancesTag);

        // Save cooldowns
        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<UUID, Map<ResourceLocation, Long>> entry : playerCooldowns.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            for (Map.Entry<ResourceLocation, Long> cooldown : entry.getValue().entrySet()) {
                playerTag.putLong(cooldown.getKey().toString(), cooldown.getValue());
            }
            cooldownsTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("Cooldowns", cooldownsTag);

        return tag;
    }
}
