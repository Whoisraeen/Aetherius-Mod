package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Handles buff effects (positive status effects).
 * Uses vanilla effects as placeholders.
 */
public class BuffEffectHandler implements IEffectHandler {
    public static final BuffEffectHandler INSTANCE = new BuffEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        int durationTicks = effect.duration();
        int amplifier = Math.max(0, (int) (effect.basePower() / 10));

        for (LivingEntity target : context.getAffectedEntities()) {
            // Apply strength buff as example
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, amplifier));
        }
    }
}
