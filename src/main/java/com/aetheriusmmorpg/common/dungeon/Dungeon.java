package com.aetheriusmmorpg.common.dungeon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Represents a dungeon definition loaded from JSON.
 * PWI-style instanced dungeons for party gameplay.
 */
public record Dungeon(
    ResourceLocation id,
    String name,
    String description,
    DungeonDifficulty difficulty,
    int requiredLevel,
    int maxLevel,
    int minPartySize,
    int maxPartySize,
    int timeLimitMinutes,
    int cooldownHours,
    List<BossEncounter> bosses,
    DungeonRewards rewards,
    ResourceLocation structureLocation,
    ResourceLocation entranceNPC
) {

    public static final Codec<Dungeon> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Dungeon::id),
            Codec.STRING.fieldOf("name").forGetter(Dungeon::name),
            Codec.STRING.fieldOf("description").forGetter(Dungeon::description),
            DungeonDifficulty.CODEC.fieldOf("difficulty").forGetter(Dungeon::difficulty),
            Codec.INT.fieldOf("required_level").forGetter(Dungeon::requiredLevel),
            Codec.INT.fieldOf("max_level").orElse(999).forGetter(Dungeon::maxLevel),
            Codec.INT.fieldOf("min_party_size").orElse(1).forGetter(Dungeon::minPartySize),
            Codec.INT.fieldOf("max_party_size").orElse(10).forGetter(Dungeon::maxPartySize),
            Codec.INT.fieldOf("time_limit_minutes").orElse(60).forGetter(Dungeon::timeLimitMinutes),
            Codec.INT.fieldOf("cooldown_hours").orElse(24).forGetter(Dungeon::cooldownHours),
            BossEncounter.CODEC.listOf().fieldOf("bosses").forGetter(Dungeon::bosses),
            DungeonRewards.CODEC.fieldOf("rewards").forGetter(Dungeon::rewards),
            ResourceLocation.CODEC.fieldOf("structure").forGetter(Dungeon::structureLocation),
            ResourceLocation.CODEC.optionalFieldOf("entrance_npc", new ResourceLocation("aetherius:dungeon_guide")).forGetter(Dungeon::entranceNPC)
        ).apply(instance, Dungeon::new)
    );

    /**
     * Check if a player's level is within dungeon range.
     */
    public boolean isLevelInRange(int playerLevel) {
        return playerLevel >= requiredLevel && playerLevel <= maxLevel;
    }

    /**
     * Check if party size is valid for this dungeon.
     */
    public boolean isPartySizeValid(int partySize) {
        return partySize >= minPartySize && partySize <= maxPartySize;
    }

    /**
     * Get time limit in ticks.
     */
    public int getTimeLimitTicks() {
        return timeLimitMinutes * 60 * 20; // minutes * seconds * ticks
    }

    /**
     * Get cooldown in ticks.
     */
    public long getCooldownTicks() {
        return cooldownHours * 60L * 60L * 20L; // hours * minutes * seconds * ticks
    }

    public enum DungeonDifficulty {
        EASY,
        NORMAL,
        HARD,
        ELITE,
        NIGHTMARE;

        public static final Codec<DungeonDifficulty> CODEC = Codec.STRING.xmap(
            str -> DungeonDifficulty.valueOf(str.toUpperCase()),
            DungeonDifficulty::name
        );
    }

    /**
     * Boss encounter within a dungeon.
     */
    public record BossEncounter(
        String bossId,
        String bossName,
        int level,
        int phase,
        ResourceLocation spawnLocation,
        List<String> mechanics
    ) {
        public static final Codec<BossEncounter> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("boss_id").forGetter(BossEncounter::bossId),
                Codec.STRING.fieldOf("boss_name").forGetter(BossEncounter::bossName),
                Codec.INT.fieldOf("level").forGetter(BossEncounter::level),
                Codec.INT.fieldOf("phase").orElse(1).forGetter(BossEncounter::phase),
                ResourceLocation.CODEC.fieldOf("spawn_location").forGetter(BossEncounter::spawnLocation),
                Codec.STRING.listOf().fieldOf("mechanics").orElse(List.of()).forGetter(BossEncounter::mechanics)
            ).apply(instance, BossEncounter::new)
        );
    }

    /**
     * Dungeon completion rewards.
     */
    public record DungeonRewards(
        int baseExperience,
        int baseGold,
        List<LootDrop> guaranteedDrops,
        List<LootDrop> randomDrops,
        int maxRandomDrops
    ) {
        public static final Codec<DungeonRewards> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("base_experience").forGetter(DungeonRewards::baseExperience),
                Codec.INT.fieldOf("base_gold").forGetter(DungeonRewards::baseGold),
                LootDrop.CODEC.listOf().fieldOf("guaranteed_drops").orElse(List.of()).forGetter(DungeonRewards::guaranteedDrops),
                LootDrop.CODEC.listOf().fieldOf("random_drops").orElse(List.of()).forGetter(DungeonRewards::randomDrops),
                Codec.INT.fieldOf("max_random_drops").orElse(3).forGetter(DungeonRewards::maxRandomDrops)
            ).apply(instance, DungeonRewards::new)
        );
    }

    /**
     * Loot drop definition.
     */
    public record LootDrop(
        ResourceLocation itemId,
        int minQuantity,
        int maxQuantity,
        float dropChance,
        LootQuality quality
    ) {
        public static final Codec<LootDrop> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(LootDrop::itemId),
                Codec.INT.fieldOf("min_quantity").orElse(1).forGetter(LootDrop::minQuantity),
                Codec.INT.fieldOf("max_quantity").orElse(1).forGetter(LootDrop::maxQuantity),
                Codec.FLOAT.fieldOf("drop_chance").orElse(1.0f).forGetter(LootDrop::dropChance),
                LootQuality.CODEC.fieldOf("quality").orElse(LootQuality.COMMON).forGetter(LootDrop::quality)
            ).apply(instance, LootDrop::new)
        );
    }

    public enum LootQuality {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY;

        public static final Codec<LootQuality> CODEC = Codec.STRING.xmap(
            str -> LootQuality.valueOf(str.toUpperCase()),
            LootQuality::name
        );
    }
}
