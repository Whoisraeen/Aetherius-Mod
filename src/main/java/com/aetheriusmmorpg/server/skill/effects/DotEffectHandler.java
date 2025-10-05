package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles damage-over-time effects.
 * Currently uses vanilla poison effect as placeholder.
 */
public class DotEffectHandler implements IEffectHandler {
    public static final DotEffectHandler INSTANCE = new DotEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        int durationTicks = effect.duration();
        int amplifier = (int) (effect.basePower() / 2); // Simplified

        for (LivingEntity target : context.getAffectedEntities()) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, durationTicks, amplifier));
        }
    }
}
