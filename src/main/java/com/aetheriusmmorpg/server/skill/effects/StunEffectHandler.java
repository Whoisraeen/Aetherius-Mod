package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles stun effects (crowd control).
 * Uses slowness + mining fatigue as stun simulation.
 */
public class StunEffectHandler implements IEffectHandler {
    public static final StunEffectHandler INSTANCE = new StunEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        int durationTicks = effect.duration();

        for (LivingEntity target : context.getAffectedEntities()) {
            // Simulate stun with multiple debuffs
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, durationTicks, 10));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, durationTicks, 10));
        }
    }
}
