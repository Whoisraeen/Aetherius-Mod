package com.aetheriusmmorpg.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Represents a starting city location for races.
 * Loaded from datapack JSON files.
 */
public record StartingCity(
    ResourceLocation id,
    String name,
    String description,
    BlockPos position,
    float yaw,  // Looking direction
    float pitch,
    ResourceKey<Level> dimension  // Which dimension (overworld, nether, etc.)
) {

    public static final Codec<StartingCity> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(StartingCity::id),
            Codec.STRING.fieldOf("name").forGetter(StartingCity::name),
            Codec.STRING.fieldOf("description").forGetter(StartingCity::description),
            BlockPos.CODEC.fieldOf("position").forGetter(StartingCity::position),
            Codec.FLOAT.fieldOf("yaw").orElse(0.0f).forGetter(StartingCity::yaw),
            Codec.FLOAT.fieldOf("pitch").orElse(0.0f).forGetter(StartingCity::pitch),
            ResourceKey.codec(net.minecraft.core.registries.Registries.DIMENSION)
                .fieldOf("dimension")
                .orElse(Level.OVERWORLD)
                .forGetter(StartingCity::dimension)
        ).apply(instance, StartingCity::new)
    );
}

