package com.aetheriusmmorpg.common.entity;

import com.aetheriusmmorpg.common.entity.hostile.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Collection of all custom Aetherius mob classes.
 * Each mob has unique abilities and fits a specific level range.
 */
public class AetheriusMobs {

    // ==================== LEVEL 10-20 MOBS ====================

    /**
     * Gloom Spider (Level 10) - Venomous spider that shoots web projectiles
     */
    public static class GloomSpider extends AetheriusMob {
        public GloomSpider(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(10);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
        }

        @Override
        protected void useSpecialAbility() {
            LivingEntity target = this.getTarget();
            if (target != null) {
                // Inject poison
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                this.playSound(SoundEvents.SPIDER_HURT, 1.0F, 0.8F);
            }
        }
    }

    /**
     * Cursed Soul (Level 15) - Ghostly attacker with possession ability
     */
    public static class CursedSoul extends AetheriusMob {
        public CursedSoul(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(15);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
        }

        @Override
        protected void useSpecialAbility() {
            LivingEntity target = this.getTarget();
            if (target != null) {
                // Drain life
                target.hurt(this.damageSources().magic(), 4.0F);
                this.heal(2.0F);

                // Spawn soul particles
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.SOUL,
                        target.getX(), target.getY() + 1, target.getZ(),
                        (this.getX() - target.getX()) * 0.1, 0.1, (this.getZ() - target.getZ()) * 0.1);
                }
            }
        }
    }

    /**
     * Vengeful Shade (Level 20) - Phases through walls
     */
    public static class VengefulShade extends AetheriusMob {
        private int phaseTicks = 0;

        public VengefulShade(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(20);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D);
        }

        @Override
        protected void useSpecialAbility() {
            if (phaseTicks == 0) {
                this.noPhysics = true;
                this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0));
                phaseTicks = 40;
            }
        }

        @Override
        public void tick() {
            super.tick();
            if (phaseTicks > 0) {
                phaseTicks--;
                if (phaseTicks == 0) {
                    this.noPhysics = false;
                }
            }
        }
    }

    // ==================== LEVEL 25-35 MOBS ====================

    /**
     * Corrupted Dryad (Level 25) - Summons poisonous vines
     */
    public static class CorruptedDryad extends AetheriusMob {
        public CorruptedDryad(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(25);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
        }

        @Override
        protected void useSpecialAbility() {
            // Poison nearby enemies
            this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(5.0D))
                .stream()
                .filter(entity -> entity != this)
                .forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                });

            this.playSound(SoundEvents.GRASS_BREAK, 2.0F, 0.5F);
        }
    }

    /**
     * Ember Imp (Level 30) - Teleporting fire thrower
     */
    public static class EmberImp extends AetheriusMob {
        public EmberImp(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(30);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D);
        }

        @Override
        protected void useSpecialAbility() {
            if (this.getTarget() != null) {
                // Throw fireball
                Vec3 lookVec = this.getViewVector(1.0F);
                SmallFireball fireball = new SmallFireball(this.level(), this,
                    lookVec.x, lookVec.y, lookVec.z);
                fireball.setPos(this.getX(), this.getY() + 1.5, this.getZ());
                this.level().addFreshEntity(fireball);

                // Teleport away
                if (this.random.nextFloat() < 0.3F) {
                    double x = this.getX() + (this.random.nextDouble() - 0.5) * 8.0D;
                    double z = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0D;
                    this.teleportTo(x, this.getY(), z);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
        }
    }

    // ==================== LEVEL 40-50 MOBS ====================

    /**
     * Rock Golem (Level 40) - Earth attacks, rock spikes
     */
    public static class RockGolem extends AetheriusMob {
        public RockGolem(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(40);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
        }

        @Override
        protected void useSpecialAbility() {
            // Ground slam - damage all nearby
            this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(4.0D))
                .stream()
                .filter(entity -> entity != this && entity.onGround())
                .forEach(entity -> {
                    entity.hurt(this.damageSources().mobAttack(this), 10.0F);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.5, 0));
                });

            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 0.5F);

            // Spawn explosion particles
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.EXPLOSION,
                    this.getX() + (this.random.nextDouble() - 0.5) * 4,
                    this.getY(),
                    this.getZ() + (this.random.nextDouble() - 0.5) * 4,
                    0, 0.1, 0);
            }
        }
    }

    /**
     * Storm Elemental (Level 50) - Lightning attacks
     */
    public static class StormElemental extends AetheriusMob {
        public StormElemental(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(50);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D);
        }

        @Override
        protected void useSpecialAbility() {
            if (this.getTarget() != null) {
                // Strike with lightning
                net.minecraft.world.entity.LightningBolt lightning =
                    EntityType.LIGHTNING_BOLT.create(this.level());
                if (lightning != null) {
                    lightning.moveTo(this.getTarget().position());
                    this.level().addFreshEntity(lightning);
                }

                // Electric particles
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY() + 1, this.getZ(),
                        (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.5);
                }
            }
        }
    }

    // ==================== LEVEL 60-70 MOBS ====================

    /**
     * Spectral Banshee (Level 60) - Debuff screams
     */
    public static class SpectralBanshee extends AetheriusMob {
        public SpectralBanshee(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(60);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D);
        }

        @Override
        protected void useSpecialAbility() {
            // Wailing scream - debuff all nearby
            this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(8.0D))
                .stream()
                .filter(entity -> entity != this)
                .forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                });

            this.playSound(SoundEvents.WARDEN_ROAR, 2.0F, 1.5F);
        }
    }

    /**
     * Skeletal Legionnaire (Level 70) - Summons minions
     */
    public static class SkeletalLegionnaire extends AetheriusMob {
        public SkeletalLegionnaire(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(70);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 10.0D);
        }

        @Override
        protected void useSpecialAbility() {
            // Summon skeleton minions
            for (int i = 0; i < 2; i++) {
                net.minecraft.world.entity.monster.Skeleton skeleton =
                    EntityType.SKELETON.create(this.level());
                if (skeleton != null) {
                    skeleton.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 2,
                        this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 2, 0, 0);
                    skeleton.setTarget(this.getTarget());
                    this.level().addFreshEntity(skeleton);
                }
            }

            this.playSound(SoundEvents.SKELETON_AMBIENT, 1.5F, 0.5F);
        }
    }

    // ==================== LEVEL 80-90 MOBS ====================

    /**
     * Clockwork Sentinel (Level 80) - Precision mechanical attacks
     */
    public static class ClockworkSentinel extends AetheriusMob {
        public ClockworkSentinel(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(80);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .add(Attributes.ARMOR, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
        }

        @Override
        protected void useSpecialAbility() {
            if (this.getTarget() != null) {
                // Rapid precision strikes
                for (int i = 0; i < 3; i++) {
                    this.getTarget().hurt(this.damageSources().mobAttack(this), 8.0F);
                }

                this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 2.0F);
            }
        }
    }

    /**
     * Mystic Treant (Level 90) - Self-healing nature magic
     */
    public static class MysticTreant extends AetheriusMob {
        public MysticTreant(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(90);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.ATTACK_DAMAGE, 28.0D)
                .add(Attributes.ARMOR, 12.0D);
        }

        @Override
        protected void useSpecialAbility() {
            // Self heal
            if (this.getHealth() < this.getMaxHealth() * 0.5) {
                this.heal(30.0F);

                // Healing particles
                for (int i = 0; i < 15; i++) {
                    this.level().addParticle(ParticleTypes.HEART,
                        this.getX() + (this.random.nextDouble() - 0.5),
                        this.getY() + this.random.nextDouble() * 2,
                        this.getZ() + (this.random.nextDouble() - 0.5),
                        0, 0.1, 0);
                }

                this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            }
        }
    }

    // ==================== LEVEL 100 ELITE MOBS ====================

    /**
     * Celestial Seraph (Level 100) - Divine holy attacks and blessings
     */
    public static class CelestialSeraph extends AetheriusMob {
        public CelestialSeraph(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
            this.setMobLevel(100);
            this.setMobTier("elite");
        }

        public static AttributeSupplier.Builder createAttributes() {
            return AetheriusMob.createAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 35.0D)
                .add(Attributes.ARMOR, 20.0D);
        }

        @Override
        protected void useSpecialAbility() {
            if (this.getTarget() != null) {
                // Holy light beam
                this.getTarget().hurt(this.damageSources().magic(), 25.0F);
                this.getTarget().setSecondsOnFire(5);

                // Divine particles
                for (int i = 0; i < 30; i++) {
                    this.level().addParticle(ParticleTypes.END_ROD,
                        this.getTarget().getX(), this.getTarget().getY() + 1, this.getTarget().getZ(),
                        (this.random.nextDouble() - 0.5) * 0.3,
                        this.random.nextDouble() * 0.5,
                        (this.random.nextDouble() - 0.5) * 0.3);
                }

                this.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.5F);
            }
        }
    }
}
