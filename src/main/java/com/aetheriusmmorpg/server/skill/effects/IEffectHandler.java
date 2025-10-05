package com.aetheriusmmorpg.server.skill.effects;

import com.aetheriusmmorpg.common.rpg.skill.SkillContext;
import com.aetheriusmmorpg.common.rpg.skill.SkillEffect;

/**
 * Interface for skill effect handlers.
 * Each effect type has its own handler implementation.
 */
public interface IEffectHandler {
    /**
     * Apply this effect to all affected entities in the context.
     */
    void apply(SkillContext context, SkillEffect effect);
}
