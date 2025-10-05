package com.aetheriusmmorpg.common.entity.hostile;

import com.aetheriusmmorpg.common.entity.AetheriusMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Frostbite Elemental - Born from icy depths of Frostfall Tundra.
 * Exudes freezing cold aura, slowing and immobilizing targets.
 * Attacks with sharp ice shards and can summon blizzards.
 */
public class FrostbiteElementalEntity extends AetheriusMob {

    private int blizzardTicks = 0;

    public FrostbiteElementalEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMobLevel(20);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AetheriusMob.createAttributes()
            .add(Attributes.MAX_HEALTH, 80.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.20D)
            .add(Attributes.ATTACK_DAMAGE, 10.0D)
            .add(Attributes.ARMOR, 5.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void useSpecialAbility() {
        if (this.getTarget() != null) {
            double distance = this.distanceTo(this.getTarget());

            if (distance < 8.0D) {
                // Close range: Summon blizzard
                summonBlizzard();
            } else {
                // Long range: Shoot ice shard
                shootIceShard();
            }
        }
    }

    private void shootIceShard() {
        if (this.getTarget() == null) return;

        Vec3 lookVec = this.getViewVector(1.0F);

        // Create ice projectile (using snowball as base, will be customized)
        net.minecraft.world.entity.projectile.Snowball iceShard =
            new net.minecraft.world.entity.projectile.Snowball(this.level(), this);

        double targetX = this.getTarget().getX() - this.getX();
        double targetY = this.getTarget().getY(0.5) - iceShard.getY();
        double targetZ = this.getTarget().getZ() - this.getZ();

        iceShard.shoot(targetX, targetY, targetZ, 1.5F, 1.0F);
        this.level().addFreshEntity(iceShard);

        // Visual and sound
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.8F);

        for (int i = 0; i < 3; i++) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                this.getX() + lookVec.x,
                this.getY() + 1.5 + lookVec.y,
                this.getZ() + lookVec.z,
                lookVec.x * 0.5,
                lookVec.y * 0.5,
                lookVec.z * 0.5);
        }
    }

    private void summonBlizzard() {
        blizzardTicks = 100; // 5 second blizzard

        // Visual effect
        this.playSound(SoundEvents.WOLF_HOWL, 1.5F, 0.5F);

        // Freeze ground in radius
        BlockPos pos = this.blockPosition();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (this.level().getBlockState(checkPos).isAir() &&
                    this.level().getBlockState(checkPos.below()).isSolid()) {
                    // Temporarily place snow
                    if (this.random.nextInt(3) == 0) {
                        this.level().setBlock(checkPos, Blocks.SNOW.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Freezing aura - slow nearby entities
        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(5.0D))
                .stream()
                .filter(entity -> entity != this && entity.distanceTo(this) < 5.0D)
                .forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0));
                });
        }

        // Blizzard effects
        if (blizzardTicks > 0) {
            blizzardTicks--;

            // Spawn snow particles
            if (this.level().isClientSide) {
                for (int i = 0; i < 3; i++) {
                    double radius = 4.0D;
                    double angle = this.random.nextDouble() * Math.PI * 2;
                    double x = this.getX() + Math.cos(angle) * radius * this.random.nextDouble();
                    double z = this.getZ() + Math.sin(angle) * radius * this.random.nextDouble();
                    double y = this.getY() + 3 + this.random.nextDouble() * 2;

                    this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        x, y, z,
                        0, -0.3, 0);
                }
            }

            // Damage entities in blizzard
            if (!this.level().isClientSide && this.tickCount % 10 == 0) {
                this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(4.0D))
                    .stream()
                    .filter(entity -> entity != this)
                    .forEach(entity -> {
                        entity.hurt(this.damageSources().freeze(), 3.0F);
                        entity.setTicksFrozen(entity.getTicksFrozen() + 40);
                    });
            }
        }

        // Ambient frost particles
        if (this.level().isClientSide && this.random.nextInt(2) == 0) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                this.getY() + this.random.nextDouble() * this.getBbHeight(),
                this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                0, 0.05, 0);
        }
    }

    @Override
    protected int getAbilityInterval() {
        return 60; // 3 seconds
    }

    @Override
    protected int getAbilityCooldown() {
        return 120; // 6 seconds
    }

    @Override
    public boolean isSensitiveToWater() {
        return false; // Ice elementals love water
    }

    @Override
    public boolean fireImmune() {
        return false; // Weak to fire
    }
}
