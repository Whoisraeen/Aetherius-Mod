package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Handles knockback effects.
 */
public class KnockbackEffectHandler implements IEffectHandler {
    public static final KnockbackEffectHandler INSTANCE = new KnockbackEffectHandler();

    @Override
    public void apply(SkillContext context, SkillEffect effect) {
        LivingEntity caster = context.getCaster();
        double knockbackPower = effect.basePower() * 0.1;

        for (LivingEntity target : context.getAffectedEntities()) {
            Vec3 direction = target.position().subtract(caster.position()).normalize();
            target.knockback(knockbackPower, -direction.x, -direction.z);
        }
    }
}
