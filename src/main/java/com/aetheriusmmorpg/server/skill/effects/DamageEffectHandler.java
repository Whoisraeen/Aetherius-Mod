package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles instant damage effects.
 * Supports critical strikes based on caster's crit rate.
 */
public class DamageEffectHandler implements IEffectHandler {
    public static final DamageEffectHandler INSTANCE = new DamageEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        double casterStat = context.getCasterAttribute(effect.scalingStat());
        double baseDamage = effect.calculatePower(casterStat);

        // Check for critical hit
        boolean isCrit = context.rollCriticalHit();
        if (isCrit) {
            baseDamage *= context.getCriticalMultiplier();
        }

        float damage = (float) baseDamage;
        DamageSource source = context.getLevel().damageSources().magic();

        for (LivingEntity target : context.getAffectedEntities()) {
            target.hurt(source, damage);

            // Send visual feedback for crits (yellow damage numbers, etc.)
            if (isCrit && context.getCaster() instanceof net.minecraft.world.entity.player.Player player) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("✦ CRITICAL! ✦").withStyle(net.minecraft.ChatFormatting.GOLD),
                    true
                );
            }
        }
    }
}
