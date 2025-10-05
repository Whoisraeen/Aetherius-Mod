package com.aetheriusmmorpg.common.entity.hostile;

import com.aetheriusmmorpg.common.entity.AetheriusMob;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Shadow Wraith - A sinister elusive creature that lurks in dark corners.
 * Can phase through walls and attacks with dark energy, draining life force.
 */
public class ShadowWraithEntity extends AetheriusMob {

    private int phaseTicks = 0;
    private boolean isPhasing = false;

    public ShadowWraithEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMobLevel(10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AetheriusMob.createAttributes()
            .add(Attributes.MAX_HEALTH, 45.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.30D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D)
            .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void useSpecialAbility() {
        if (this.getTarget() != null && !isPhasing) {
            // Phase ability: become incorporeal and move through blocks
            activatePhase();
        }
    }

    private void activatePhase() {
        isPhasing = true;
        phaseTicks = 60; // 3 seconds of phasing

        // Visual effect
        this.level().addParticle(ParticleTypes.SMOKE,
            this.getX(), this.getY() + 1, this.getZ(),
            0, 0.5, 0);

        // Play sound
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.8F);

        // Temporary invisibility and no-clip
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();

        // Handle phasing
        if (isPhasing) {
            phaseTicks--;

            // Spawn dark particles
            if (this.level().isClientSide && this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5),
                    this.getY() + this.random.nextDouble() * 2,
                    this.getZ() + (this.random.nextDouble() - 0.5),
                    0, 0.05, 0);
            }

            // Move toward target
            if (this.getTarget() != null) {
                Vec3 targetPos = this.getTarget().position();
                Vec3 direction = targetPos.subtract(this.position()).normalize();
                this.setDeltaMovement(direction.scale(0.3));
            }

            if (phaseTicks <= 0) {
                deactivatePhase();
            }
        }

        // Life drain aura when near players
        if (!this.level().isClientSide && this.getTarget() != null) {
            double distance = this.distanceTo(this.getTarget());
            if (distance < 3.0D && this.tickCount % 20 == 0) {
                // Drain 1 health per second when close
                this.getTarget().hurt(this.damageSources().magic(), 2.0F);
                this.heal(1.0F);

                // Particles
                this.level().addParticle(ParticleTypes.SOUL,
                    this.getTarget().getX(), this.getTarget().getY() + 1, this.getTarget().getZ(),
                    (this.getX() - this.getTarget().getX()) * 0.1,
                    0.1,
                    (this.getZ() - this.getTarget().getZ()) * 0.1);
            }
        }
    }

    private void deactivatePhase() {
        isPhasing = false;
        this.noPhysics = false;

        // Visual effect
        this.level().addParticle(ParticleTypes.LARGE_SMOKE,
            this.getX(), this.getY() + 1, this.getZ(),
            0, 0, 0);

        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Reduced damage while phasing
        if (isPhasing) {
            amount *= 0.5F;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected int getAbilityInterval() {
        return 80; // 4 seconds
    }

    @Override
    protected int getAbilityCooldown() {
        return 160; // 8 seconds
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, looting, wasRecentlyHit);
        // TODO: Drop spectral essence
    }
}
