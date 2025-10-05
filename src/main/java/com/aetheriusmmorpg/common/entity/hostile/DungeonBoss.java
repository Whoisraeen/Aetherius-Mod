package com.aetheriusmmorpg.common.entity.hostile;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.dungeon.Dungeon;
import com.aetheriusmmorpg.common.dungeon.DungeonInstance;
import com.aetheriusmmorpg.common.dungeon.DungeonManager;
import com.aetheriusmmorpg.common.entity.AetheriusMob;
import com.aetheriusmmorpg.common.party.PartyManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * Base class for dungeon bosses with multi-phase mechanics and enrage timers.
 */
public class DungeonBoss extends AetheriusMob {

    private static final EntityDataAccessor<Integer> CURRENT_PHASE =
        SynchedEntityData.defineId(DungeonBoss.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> ENRAGED =
        SynchedEntityData.defineId(DungeonBoss.class, EntityDataSerializers.BOOLEAN);

    protected UUID dungeonInstanceId;
    protected String bossId;

    private int enrageTimer = 0;
    private int enrageTimerMax = 12000; // 10 minutes default

    public DungeonBoss(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMobTier("boss");
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CURRENT_PHASE, 1);
        this.entityData.define(ENRAGED, false);
    }

    public static AttributeSupplier.Builder createBossAttributes() {
        return createAttributes()
            .add(Attributes.MAX_HEALTH, 500.0D)
            .add(Attributes.ATTACK_DAMAGE, 15.0D)
            .add(Attributes.ARMOR, 10.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    /**
     * Set the dungeon instance this boss belongs to.
     */
    public void setDungeonInstance(UUID instanceId, String bossId) {
        this.dungeonInstanceId = instanceId;
        this.bossId = bossId;
    }

    /**
     * Get current phase.
     */
    public int getCurrentPhase() {
        return this.entityData.get(CURRENT_PHASE);
    }

    /**
     * Set current phase.
     */
    public void setCurrentPhase(int phase) {
        this.entityData.set(CURRENT_PHASE, phase);
        onPhaseChange(phase);
    }

    /**
     * Check if boss is enraged.
     */
    public boolean isEnraged() {
        return this.entityData.get(ENRAGED);
    }

    /**
     * Trigger enrage.
     */
    protected void triggerEnrage() {
        this.entityData.set(ENRAGED, true);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
            this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * 2
        );
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
            this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 1.5
        );

        // Notify players
        if (!level().isClientSide) {
            level().players().forEach(player -> {
                if (player instanceof ServerPlayer && distanceTo(player) < 64) {
                    player.sendSystemMessage(Component.literal("§c§l" + getName().getString() + " has ENRAGED!"));
                }
            });
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // Check health for phase changes
            float healthPercent = getHealth() / getMaxHealth();

            if (healthPercent <= 0.75f && getCurrentPhase() == 1) {
                setCurrentPhase(2);
            } else if (healthPercent <= 0.5f && getCurrentPhase() == 2) {
                setCurrentPhase(3);
            } else if (healthPercent <= 0.25f && getCurrentPhase() == 3) {
                setCurrentPhase(4);
            }

            // Enrage timer
            if (!isEnraged() && enrageTimerMax > 0) {
                enrageTimer++;
                if (enrageTimer >= enrageTimerMax) {
                    triggerEnrage();
                }
            }
        }
    }

    /**
     * Called when phase changes.
     * Override in subclasses for phase-specific mechanics.
     */
    protected void onPhaseChange(int newPhase) {
        if (!level().isClientSide) {
            level().players().forEach(player -> {
                if (player instanceof ServerPlayer && distanceTo(player) < 64) {
                    player.sendSystemMessage(Component.literal(
                        "§6" + getName().getString() + " §fhas entered §cPhase " + newPhase + "§f!"
                    ));
                }
            });
        }
    }

    @Override
    protected void useSpecialAbility() {
        // Override in subclasses for phase-specific abilities
        int phase = getCurrentPhase();
        switch (phase) {
            case 1: usePhase1Ability(); break;
            case 2: usePhase2Ability(); break;
            case 3: usePhase3Ability(); break;
            case 4: usePhase4Ability(); break;
        }
    }

    protected void usePhase1Ability() {
        // Override in subclasses
    }

    protected void usePhase2Ability() {
        // Override in subclasses
    }

    protected void usePhase3Ability() {
        // Override in subclasses
    }

    protected void usePhase4Ability() {
        // Override in subclasses
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!level().isClientSide && dungeonInstanceId != null && bossId != null) {
            // Mark boss as defeated in dungeon instance
            if (this.getServer() != null) {
                DungeonManager manager = DungeonManager.get(this.getServer());
                DungeonInstance instance = manager.getInstance(dungeonInstanceId);

                if (instance != null) {
                    instance.defeatBoss(bossId);

                    // Check if this was the last boss
                    Dungeon dungeon = DungeonManager.getDungeon(instance.getDungeonId());
                    if (dungeon != null && instance.areAllBossesDefeated(dungeon)) {
                        completeDungeon(manager, instance, dungeon);
                    }
                }
            }
        }
    }

    /**
     * Complete the dungeon and distribute rewards.
     */
    private void completeDungeon(DungeonManager manager, DungeonInstance instance, Dungeon dungeon) {
        if (this.getServer() == null) return;

        // Notify all participants
        for (UUID participantId : instance.getParticipants()) {
            ServerPlayer player = this.getServer().getPlayerList().getPlayer(participantId);
            if (player != null) {
                player.sendSystemMessage(Component.literal("§6§l=== DUNGEON COMPLETE ==="));
                player.sendSystemMessage(Component.literal("§aCongratulations! You have completed " + dungeon.name() + "!"));

                // Give rewards
                giveRewards(player, dungeon);
            }
        }

        // Complete the instance
        manager.completeInstance(instance.getInstanceId(), this.getServer());

        AetheriusMod.LOGGER.info("Dungeon {} completed by party {}",
            dungeon.id(), instance.getPartyId());
    }

    /**
     * Give rewards to a player.
     */
    private void giveRewards(ServerPlayer player, Dungeon dungeon) {
        Dungeon.DungeonRewards rewards = dungeon.rewards();

        // XP reward
        player.getCapability(com.aetheriusmmorpg.common.capability.player.PlayerRpgData.CAPABILITY)
            .ifPresent(data -> {
                data.addExperience(rewards.baseExperience());
                player.sendSystemMessage(Component.literal("§a+§f" + rewards.baseExperience() + " XP"));
            });

        // Gold reward
        player.getCapability(com.aetheriusmmorpg.common.capability.player.PlayerRpgData.CAPABILITY)
            .ifPresent(data -> {
                data.addGold(rewards.baseGold());
                player.sendSystemMessage(Component.literal("§6+§f" + rewards.baseGold() + " Gold"));
            });

        // Item drops (guaranteed)
        for (Dungeon.LootDrop drop : rewards.guaranteedDrops()) {
            int quantity = drop.minQuantity();
            com.aetheriusmmorpg.common.util.ItemRewardUtil.giveItems(player, drop.itemId(), quantity);
            player.sendSystemMessage(Component.literal("§b+§f " + drop.itemId() + " x" + quantity));
        }

        // Random drops
        int dropsGiven = 0;
        for (Dungeon.LootDrop drop : rewards.randomDrops()) {
            if (dropsGiven >= rewards.maxRandomDrops()) break;

            if (this.random.nextFloat() <= drop.dropChance()) {
                int quantity = drop.minQuantity() + this.random.nextInt(drop.maxQuantity() - drop.minQuantity() + 1);
                com.aetheriusmmorpg.common.util.ItemRewardUtil.giveItems(player, drop.itemId(), quantity);
                player.sendSystemMessage(Component.literal(
                    "§d+" + drop.quality().name() + " §f" + drop.itemId() + " x" + quantity
                ));
                dropsGiven++;
            }
        }
    }

    /**
     * Set enrage timer in minutes.
     */
    public void setEnrageTimerMinutes(int minutes) {
        this.enrageTimerMax = minutes * 60 * 20; // minutes * seconds * ticks
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CurrentPhase", getCurrentPhase());
        tag.putBoolean("Enraged", isEnraged());
        tag.putInt("EnrageTimer", enrageTimer);
        tag.putInt("EnrageTimerMax", enrageTimerMax);

        if (dungeonInstanceId != null) {
            tag.putUUID("DungeonInstanceId", dungeonInstanceId);
        }
        if (bossId != null) {
            tag.putString("BossId", bossId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCurrentPhase(tag.getInt("CurrentPhase"));
        this.entityData.set(ENRAGED, tag.getBoolean("Enraged"));
        this.enrageTimer = tag.getInt("EnrageTimer");
        this.enrageTimerMax = tag.getInt("EnrageTimerMax");

        if (tag.contains("DungeonInstanceId")) {
            this.dungeonInstanceId = tag.getUUID("DungeonInstanceId");
        }
        if (tag.contains("BossId")) {
            this.bossId = tag.getString("BossId");
        }
    }
}
