package com.aetheriusmmorpg.common.entity.hostile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shadow Lord - Example dungeon boss with 4-phase mechanics.
 *
 * Phase 1 (100-75%): Basic attacks + shadow bolt
 * Phase 2 (75-50%): Summons shadow minions
 * Phase 3 (50-25%): AOE darkness + teleportation
 * Phase 4 (25-0%): Ultimate shadow form with all abilities
 */
public class ShadowLordBoss extends DungeonBoss {

    private int shadowBoltCooldown = 0;
    private int summonCooldown = 0;
    private int teleportCooldown = 0;

    public ShadowLordBoss(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMobLevel(30);
        this.setEnrageTimerMinutes(15); // 15-minute enrage timer
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes()
            .add(Attributes.MAX_HEALTH, 800.0D)
            .add(Attributes.ATTACK_DAMAGE, 20.0D)
            .add(Attributes.ARMOR, 15.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            // Cooldown management
            if (shadowBoltCooldown > 0) shadowBoltCooldown--;
            if (summonCooldown > 0) summonCooldown--;
            if (teleportCooldown > 0) teleportCooldown--;

            // Ambient shadow particles
            if (this.random.nextFloat() < 0.3f) {
                ((ServerLevel) level()).sendParticles(
                    ParticleTypes.SMOKE,
                    getX() + (random.nextDouble() - 0.5) * 2,
                    getY() + random.nextDouble() * 2,
                    getZ() + (random.nextDouble() - 0.5) * 2,
                    1, 0, 0, 0, 0
                );
            }
        }
    }

    @Override
    protected void usePhase1Ability() {
        // Shadow Bolt - Single target dark magic
        LivingEntity target = getTarget();
        if (shadowBoltCooldown <= 0 && target != null) {
            castShadowBolt(target);
            shadowBoltCooldown = 60; // 3 seconds
        }
    }

    @Override
    protected void usePhase2Ability() {
        // Shadow Bolt + Summon Minions
        usePhase1Ability();

        if (summonCooldown <= 0) {
            summonShadowMinions();
            summonCooldown = 200; // 10 seconds
        }
    }

    @Override
    protected void usePhase3Ability() {
        // All previous abilities + Darkness AOE + Teleportation
        usePhase2Ability();

        if (teleportCooldown <= 0) {
            castDarknessAOE();
            teleportToRandomLocation();
            teleportCooldown = 100; // 5 seconds
        }
    }

    @Override
    protected void usePhase4Ability() {
        // Ultimate phase - all abilities with reduced cooldowns
        LivingEntity target = getTarget();
        if (shadowBoltCooldown <= 0 && target != null) {
            castShadowBolt(target);
            castShadowBolt(target); // Double cast!
            shadowBoltCooldown = 40; // Faster cooldown
        }

        if (summonCooldown <= 0) {
            summonShadowMinions();
            summonShadowMinions(); // Double summon!
            summonCooldown = 150;
        }

        if (teleportCooldown <= 0) {
            castDarknessAOE();
            teleportToRandomLocation();
            teleportCooldown = 60; // Faster teleport
        }
    }

    /**
     * Cast Shadow Bolt at target.
     */
    private void castShadowBolt(LivingEntity target) {
        if (level().isClientSide) return;

        // Damage and slow
        target.hurt(damageSources().mobAttack(this), 10.0f);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));

        // Particle effect
        ((ServerLevel) level()).sendParticles(
            ParticleTypes.SMOKE,
            target.getX(), target.getY() + 1, target.getZ(),
            20, 0.5, 0.5, 0.5, 0.1
        );

        playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 0.8f);
    }

    /**
     * Summon shadow minions to aid in combat.
     */
    private void summonShadowMinions() {
        if (level().isClientSide) return;

        int count = getCurrentPhase() >= 4 ? 4 : 2;

        for (int i = 0; i < count; i++) {
            // Spawn position around boss
            double angle = (Math.PI * 2 / count) * i;
            double x = getX() + Math.cos(angle) * 3;
            double z = getZ() + Math.sin(angle) * 3;

            // TODO: Spawn actual shadow minion entity
            // For now, just visual effect
            ((ServerLevel) level()).sendParticles(
                ParticleTypes.LARGE_SMOKE,
                x, getY(), z,
                30, 0.5, 1, 0.5, 0.1
            );
        }

        playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 0.7f);
    }

    /**
     * Cast AOE darkness that blinds all nearby players.
     */
    private void castDarknessAOE() {
        if (level().isClientSide) return;

        AABB area = new AABB(getX() - 10, getY() - 5, getZ() - 10,
                             getX() + 10, getY() + 5, getZ() + 10);

        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            if (entity != this && entity instanceof net.minecraft.world.entity.player.Player) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
            }
        }

        // Massive particle effect
        ((ServerLevel) level()).sendParticles(
            ParticleTypes.SQUID_INK,
            getX(), getY() + 1, getZ(),
            100, 5, 2, 5, 0.3
        );

        playSound(SoundEvents.WARDEN_SONIC_BOOM, 1.0f, 0.5f);
    }

    /**
     * Teleport to a random location near the arena.
     */
    private void teleportToRandomLocation() {
        if (level().isClientSide) return;

        // Teleport effect at old location
        ((ServerLevel) level()).sendParticles(
            ParticleTypes.PORTAL,
            getX(), getY() + 1, getZ(),
            50, 0.5, 1, 0.5, 0.5
        );

        // Random location within 15 blocks
        double newX = getX() + (random.nextDouble() - 0.5) * 30;
        double newZ = getZ() + (random.nextDouble() - 0.5) * 30;

        teleportTo(newX, getY(), newZ);

        // Teleport effect at new location
        ((ServerLevel) level()).sendParticles(
            ParticleTypes.PORTAL,
            getX(), getY() + 1, getZ(),
            50, 0.5, 1, 0.5, 0.5
        );

        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 0.8f);
    }

    @Override
    protected void onPhaseChange(int newPhase) {
        super.onPhaseChange(newPhase);

        if (!level().isClientSide) {
            // Heal slightly on phase change
            heal(getMaxHealth() * 0.1f);

            // Lightning effect
            ((ServerLevel) level()).sendParticles(
                ParticleTypes.FLASH,
                getX(), getY() + 1, getZ(),
                50, 1, 1, 1, 0
            );

            playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
    }
}
