package com.aetheriusmmorpg.common.rpg.clazz;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Represents a playable class in Aetherius.
 * Loaded from datapack JSON files.
 */
public record PlayerClass(
    ResourceLocation id,
    String name,
    String description,
    ClassRole role,  // TANK, HEALER, DPS, SUPPORT
    Map<String, Double> attributeGrowth,  // Per-level attribute bonuses
    List<ResourceLocation> availableSkills,  // Skills this class can learn
    Map<String, String> weaponProficiencies  // Allowed weapon types
) {

    public static final Codec<PlayerClass> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(PlayerClass::id),
            Codec.STRING.fieldOf("name").forGetter(PlayerClass::name),
            Codec.STRING.fieldOf("description").forGetter(PlayerClass::description),
            ClassRole.CODEC.fieldOf("role").forGetter(PlayerClass::role),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("attribute_growth").forGetter(PlayerClass::attributeGrowth),
            ResourceLocation.CODEC.listOf().fieldOf("available_skills").forGetter(PlayerClass::availableSkills),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("weapon_proficiencies").forGetter(PlayerClass::weaponProficiencies)
        ).apply(instance, PlayerClass::new)
    );

    /**
     * Get attribute growth per level, or 0.5 if not defined.
     */
    public double getAttributeGrowth(String attributeName) {
        return attributeGrowth.getOrDefault(attributeName, 0.5);
    }

    /**
     * Check if this class can learn the given skill.
     */
    public boolean canLearnSkill(ResourceLocation skillId) {
        return availableSkills.contains(skillId);
    }

    public enum ClassRole {
        TANK,
        HEALER,
        DPS,
        SUPPORT;

        public static final Codec<ClassRole> CODEC = Codec.STRING.xmap(
            str -> ClassRole.valueOf(str.toUpperCase()),
            ClassRole::name
        );
    }
}
