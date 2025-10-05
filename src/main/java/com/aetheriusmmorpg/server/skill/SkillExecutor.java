package com.aetheriusmmorpg.server.skill;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import com.aetheriusmmorpg.common.rpg.skill.*;
import com.aetheriusmmorpg.server.skill.effects.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Server-side skill execution pipeline.
 * Validates, resolves targets, and applies effects.
 */
public class SkillExecutor {

    /**
     * Execute a skill from a caster.
     * Returns true if skill was successfully cast.
     */
    public static boolean executeSkill(LivingEntity caster, Skill skill, LivingEntity target, Vec3 targetPos) {
        if (caster.level().isClientSide) {
            return false; // Server-side only
        }

        SkillContext context = new SkillContext(caster, skill, caster.level());
        context.setTarget(target);
        context.setTargetPos(targetPos);

        // Validation phase
        if (!validateSkill(context)) {
            return false;
        }

        // Target selection phase
        selectTargets(context);

        // Effect application phase
        applyEffects(context);

        // Set cooldown for player casters (with haste scaling)
        if (caster instanceof Player player && skill.cooldown() > 0) {
            player.getCapability(PlayerRpgData.CAPABILITY).ifPresent(data -> {
                long currentTick = player.level().getGameTime();
                int adjustedCooldown = context.getAdjustedCooldown();
                data.setCooldown(skill.id(), currentTick, adjustedCooldown * 20); // Convert seconds to ticks
            });
        }

        AetheriusMod.LOGGER.debug("Skill {} executed by {}", skill.id(), caster.getName().getString());
        return true;
    }

    /**
     * Validate skill can be cast (range, cost, cooldown).
     */
    private static boolean validateSkill(SkillContext context) {
        LivingEntity caster = context.getCaster();
        Skill skill = context.getSkill();

        // Check cooldown for player casters
        if (caster instanceof Player player) {
            PlayerRpgData data = player.getCapability(PlayerRpgData.CAPABILITY).orElse(null);
            if (data != null) {
                long currentTick = player.level().getGameTime();
                if (data.isOnCooldown(skill.id(), currentTick)) {
                    int remaining = data.getRemainingCooldown(skill.id(), currentTick);
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Skill on cooldown: " + (remaining / 20) + "s remaining"),
                        true
                    );
                    return false;
                }
            }
        }

        // Check range for targeted skills
        if (skill.targetType() == Skill.SkillTargetType.SINGLE && context.getTarget() != null) {
            double distance = caster.distanceTo(context.getTarget());
            if (distance > skill.range()) {
                if (caster instanceof Player player) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Target out of range!"),
                        true
                    );
                }
                return false;
            }
        }

        // Check costs (simplified - would integrate with capability system)
        Skill.CostRequirement cost = skill.cost();
        if (cost.health() > 0 && caster.getHealth() <= cost.health()) {
            return false;
        }

        return true;
    }

    /**
     * Select targets based on skill target type.
     */
    private static void selectTargets(SkillContext context) {
        Skill skill = context.getSkill();
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();

        switch (skill.targetType()) {
            case SELF -> context.addAffectedEntity(caster);

            case SINGLE -> {
                if (context.getTarget() != null) {
                    context.addAffectedEntity(context.getTarget());
                }
            }

            case AOE_CIRCLE -> {
                Vec3 center = context.getTargetPos() != null ? context.getTargetPos() : caster.position();
                double radius = skill.range();
                AABB aabb = new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb,
                    entity -> entity != caster && entity.distanceToSqr(center) <= radius * radius);
                entities.forEach(context::addAffectedEntity);
            }

            case AOE_CONE -> {
                // Simplified cone: entities in front of caster within range
                Vec3 lookVec = caster.getLookAngle();
                double radius = skill.range();
                AABB aabb = caster.getBoundingBox().inflate(radius);
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb,
                    entity -> {
                        if (entity == caster) return false;
                        Vec3 toEntity = entity.position().subtract(caster.position()).normalize();
                        double dot = lookVec.dot(toEntity);
                        return dot > 0.5; // ~60 degree cone
                    });
                entities.forEach(context::addAffectedEntity);
            }
        }
    }

    /**
     * Apply all effects to selected targets.
     */
    private static void applyEffects(SkillContext context) {
        for (SkillEffect effect : context.getSkill().effects()) {
            IEffectHandler handler = getEffectHandler(effect.type());
            if (handler != null) {
                handler.apply(context, effect);
            }
        }
    }

    /**
     * Get the appropriate effect handler for an effect type.
     */
    private static IEffectHandler getEffectHandler(SkillEffect.EffectType type) {
        return switch (type) {
            case DAMAGE -> DamageEffectHandler.INSTANCE;
            case HEAL -> HealEffectHandler.INSTANCE;
            case DOT -> DotEffectHandler.INSTANCE;
            case HOT -> HotEffectHandler.INSTANCE;
            case BUFF -> BuffEffectHandler.INSTANCE;
            case DEBUFF -> DebuffEffectHandler.INSTANCE;
            case STUN -> StunEffectHandler.INSTANCE;
            case KNOCKBACK -> KnockbackEffectHandler.INSTANCE;
        };
    }
}
