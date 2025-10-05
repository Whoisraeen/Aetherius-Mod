package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles instant heal effects.
 * Supports critical heals based on caster's crit rate.
 */
public class HealEffectHandler implements IEffectHandler {
    public static final HealEffectHandler INSTANCE = new HealEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        double casterStat = context.getCasterAttribute(effect.scalingStat());
        double baseHeal = effect.calculatePower(casterStat);

        // Check for critical heal
        boolean isCrit = context.rollCriticalHit();
        if (isCrit) {
            baseHeal *= context.getCriticalMultiplier();
        }

        float healAmount = (float) baseHeal;

        for (LivingEntity target : context.getAffectedEntities()) {
            target.heal(healAmount);

            // Send visual feedback for crits
            if (isCrit && context.getCaster() instanceof net.minecraft.world.entity.player.Player player) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("✦ CRITICAL HEAL! ✦").withStyle(net.minecraft.ChatFormatting.GREEN),
                    true
                );
            }
        }
    }
}
