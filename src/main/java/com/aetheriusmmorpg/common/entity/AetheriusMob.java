package com.aetheriusmmorpg.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base class for all Aetherius custom mobs.
 * Provides standard RPG mechanics like levels, scaling stats, and special abilities.
 */
public class AetheriusMob extends Monster {

    private static final EntityDataAccessor<Integer> MOB_LEVEL =
        SynchedEntityData.defineId(AetheriusMob.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> MOB_TYPE =
        SynchedEntityData.defineId(AetheriusMob.class, EntityDataSerializers.STRING);

    protected int abilityTicks = 0;
    protected int abilityCooldown = 0;

    public AetheriusMob(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10; // Base XP
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MOB_LEVEL, 1);
        this.entityData.define(MOB_TYPE, "common");
    }

    @Override
    protected void registerGoals() {
        // Passive goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Targeting goals
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .add(Attributes.ARMOR, 0.0D)
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // Level System
    public int getMobLevel() {
        return this.entityData.get(MOB_LEVEL);
    }

    public void setMobLevel(int level) {
        this.entityData.set(MOB_LEVEL, level);
        this.updateStatsForLevel(level);
    }

    protected void updateStatsForLevel(int level) {
        // Scale health: base + (level * 5)
        double baseHealth = 20.0D;
        double healthPerLevel = 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth + (level * healthPerLevel));
        this.setHealth(this.getMaxHealth());

        // Scale damage: base + (level * 0.5)
        double baseDamage = 3.0D;
        double damagePerLevel = 0.5D;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDamage + (level * damagePerLevel));

        // Scale armor: level * 0.2
        this.getAttribute(Attributes.ARMOR).setBaseValue(level * 0.2D);

        // Scale XP reward: base + (level * 5)
        this.xpReward = 10 + (level * 5);
    }

    // Mob Type (common, elite, boss)
    public String getMobType() {
        return this.entityData.get(MOB_TYPE);
    }

    public void setMobType(String type) {
        this.entityData.set(MOB_TYPE, type);
        applyTypeModifiers(type);
    }

    protected void applyTypeModifiers(String type) {
        switch (type.toLowerCase()) {
            case "elite":
                // 2x health and damage
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
                    this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * 2);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                    this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * 2);
                this.xpReward *= 3;
                break;
            case "boss":
                // 5x health and damage
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
                    this.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * 5);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                    this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * 3);
                this.xpReward *= 10;
                break;
            default:
                // Common mob, no modifiers
                break;
        }
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void tick() {
        super.tick();

        // Handle ability cooldowns
        if (abilityCooldown > 0) {
            abilityCooldown--;
        }

        // Trigger abilities
        if (this.getTarget() != null && abilityCooldown == 0) {
            abilityTicks++;
            if (abilityTicks >= getAbilityInterval()) {
                useSpecialAbility();
                abilityTicks = 0;
                abilityCooldown = getAbilityCooldown();
            }
        }
    }

    /**
     * Override in subclasses to implement special abilities.
     */
    protected void useSpecialAbility() {
        // Default: no special ability
    }

    /**
     * How many ticks between ability uses.
     */
    protected int getAbilityInterval() {
        return 100; // 5 seconds
    }

    /**
     * Cooldown after using ability.
     */
    protected int getAbilityCooldown() {
        return 200; // 10 seconds
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MobLevel", getMobLevel());
        tag.putString("MobType", getMobType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MobLevel")) {
            setMobLevel(tag.getInt("MobLevel"));
        }
        if (tag.contains("MobType")) {
            setMobType(tag.getString("MobType"));
        }
    }

    /**
     * Get custom display name with level and type.
     */
    @Override
    public net.minecraft.network.chat.Component getName() {
        String prefix = "";
        switch (getMobType()) {
            case "elite": prefix = "§6[Elite] "; break;
            case "boss": prefix = "§c[Boss] "; break;
        }
        return net.minecraft.network.chat.Component.literal(
            prefix + super.getName().getString() + " §7[Lv." + getMobLevel() + "]"
        );
    }
}
