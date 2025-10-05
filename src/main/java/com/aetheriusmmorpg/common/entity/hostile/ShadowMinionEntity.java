package com.aetheriusmmorpg.common.entity.hostile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Shadow Minion summoned by Shadow Lord Boss.
 * Weaker minions that attack players.
 */
public class ShadowMinionEntity extends Monster {

    public ShadowMinionEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D) // Weaker than boss
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .add(Attributes.ARMOR, 2.0D)
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Despawn if far from players (summoned minion)
        return distanceToClosestPlayer > 64.0D;
    }
}

