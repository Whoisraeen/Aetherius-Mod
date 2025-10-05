package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles debuff effects (negative status effects).
 * Uses vanilla effects as placeholders.
 */
public class DebuffEffectHandler implements IEffectHandler {
    public static final DebuffEffectHandler INSTANCE = new DebuffEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        int durationTicks = effect.duration();
        int amplifier = Math.max(0, (int) (effect.basePower() / 10));

        for (LivingEntity target : context.getAffectedEntities()) {
            // Apply weakness debuff as example
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, durationTicks, amplifier));
        }
    }
}
