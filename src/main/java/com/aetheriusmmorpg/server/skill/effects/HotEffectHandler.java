package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles heal-over-time effects.
 * Currently uses vanilla regeneration effect as placeholder.
 */
public class HotEffectHandler implements IEffectHandler {
    public static final HotEffectHandler INSTANCE = new HotEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        int durationTicks = effect.duration();
        int amplifier = (int) (effect.basePower() / 2); // Simplified

        for (LivingEntity target : context.getAffectedEntities()) {
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durationTicks, amplifier));
        }
    }
}
