package com.aetheriusmmorpg.common.rpg.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Represents a usable skill/ability in Aetherius.
 * Loaded from datapack JSON files.
 */
public record Skill(
    ResourceLocation id,
    String name,
    String description,
    SkillTargetType targetType,  // SELF, SINGLE, AOE, GROUND
    double range,  // Max range in blocks
    int cooldown,  // Cooldown in ticks (20 = 1 second)
    int castTime,  // Cast time in ticks (0 = instant)
    CostRequirement cost,  // Mana/energy cost
    List<SkillEffect> effects  // What the skill does
) {

    public static final Codec<Skill> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Skill::id),
            Codec.STRING.fieldOf("name").forGetter(Skill::name),
            Codec.STRING.fieldOf("description").forGetter(Skill::description),
            SkillTargetType.CODEC.fieldOf("target_type").forGetter(Skill::targetType),
            Codec.DOUBLE.fieldOf("range").forGetter(Skill::range),
            Codec.INT.fieldOf("cooldown").forGetter(Skill::cooldown),
            Codec.INT.optionalFieldOf("cast_time", 0).forGetter(Skill::castTime),
            CostRequirement.CODEC.fieldOf("cost").forGetter(Skill::cost),
            SkillEffect.CODEC.listOf().fieldOf("effects").forGetter(Skill::effects)
        ).apply(instance, Skill::new)
    );

    public enum SkillTargetType {
        SELF,      // Targets only the caster
        SINGLE,    // Single target (enemy or ally)
        AOE_CONE,  // Cone in front of caster
        AOE_CIRCLE, // Circle around target point
        GROUND;    // Ground-targeted AOE

        public static final Codec<SkillTargetType> CODEC = Codec.STRING.xmap(
            str -> SkillTargetType.valueOf(str.toUpperCase()),
            SkillTargetType::name
        );
    }

    public record CostRequirement(
        int health,
        int mana,
        int energy
    ) {
        public static final Codec<CostRequirement> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.optionalFieldOf("health", 0).forGetter(CostRequirement::health),
                Codec.INT.optionalFieldOf("mana", 0).forGetter(CostRequirement::mana),
                Codec.INT.optionalFieldOf("energy", 0).forGetter(CostRequirement::energy)
            ).apply(instance, CostRequirement::new)
        );
    }
}
