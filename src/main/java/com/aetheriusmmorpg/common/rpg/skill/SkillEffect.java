package com.aetheriusmmorpg.common.rpg.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a single effect applied by a skill.
 * Can be damage, heal, buff, debuff, DOT, HOT, stun, etc.
 */
public record SkillEffect(
    EffectType type,
    double basePower,  // Base value (damage/heal amount)
    double scaling,    // Attribute scaling multiplier
    String scalingStat, // Which stat to scale with (power, spirit, etc)
    int duration,      // Duration in ticks (for DOTs, HOTs, buffs)
    int tickInterval   // How often DOT/HOT ticks (0 for instant)
) {

    public static final Codec<SkillEffect> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            EffectType.CODEC.fieldOf("type").forGetter(SkillEffect::type),
            Codec.DOUBLE.fieldOf("base_power").forGetter(SkillEffect::basePower),
            Codec.DOUBLE.optionalFieldOf("scaling", 1.0).forGetter(SkillEffect::scaling),
            Codec.STRING.optionalFieldOf("scaling_stat", "power").forGetter(SkillEffect::scalingStat),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(SkillEffect::duration),
            Codec.INT.optionalFieldOf("tick_interval", 20).forGetter(SkillEffect::tickInterval)
        ).apply(instance, SkillEffect::new)
    );

    public enum EffectType {
        DAMAGE,      // Instant damage
        HEAL,        // Instant heal
        DOT,         // Damage over time
        HOT,         // Heal over time
        BUFF,        // Positive status effect
        DEBUFF,      // Negative status effect
        STUN,        // Crowd control - stun
        KNOCKBACK;   // Knockback effect

        public static final Codec<EffectType> CODEC = Codec.STRING.xmap(
            str -> EffectType.valueOf(str.toUpperCase()),
            EffectType::name
        );
    }

    /**
     * Calculate the final power of this effect based on caster's stats.
     */
    public double calculatePower(double casterStatValue) {
        return basePower + (casterStatValue * scaling);
    }
}
