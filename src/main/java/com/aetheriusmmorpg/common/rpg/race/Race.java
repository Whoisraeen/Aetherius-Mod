package com.aetheriusmmorpg.common.rpg.race;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Represents a playable race in Aetherius.
 * Loaded from datapack JSON files.
 */
public record Race(
    ResourceLocation id,
    String name,
    String description,
    Map<String, Double> baseAttributes,  // e.g., "power": 12.0
    List<ResourceLocation> allowedClasses,  // List of class IDs this race can be
    List<String> passiveAbilities,  // Innate passive bonuses
    ResourceLocation startingCity  // Starting location
) {

    public static final Codec<Race> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Race::id),
            Codec.STRING.fieldOf("name").forGetter(Race::name),
            Codec.STRING.fieldOf("description").forGetter(Race::description),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("base_attributes").forGetter(Race::baseAttributes),
            ResourceLocation.CODEC.listOf().fieldOf("allowed_classes").forGetter(Race::allowedClasses),
            Codec.STRING.listOf().fieldOf("passive_abilities").forGetter(Race::passiveAbilities),
            ResourceLocation.CODEC.fieldOf("starting_city").forGetter(Race::startingCity)
        ).apply(instance, Race::new)
    );

    /**
     * Get a base attribute value, or 10.0 if not defined.
     */
    public double getBaseAttribute(String attributeName) {
        return baseAttributes.getOrDefault(attributeName, 10.0);
    }

    /**
     * Check if this race can use the given class.
     */
    public boolean canUseClass(ResourceLocation classId) {
        return allowedClasses.contains(classId);
    }
}
