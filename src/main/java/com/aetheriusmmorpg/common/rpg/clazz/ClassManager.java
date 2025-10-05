package com.aetheriusmmorpg.common.rpg.clazz;

import com.aetheriusmmorpg.AetheriusMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages loading and accessing classes from datapacks.
 * Classes are defined in data/<namespace>/classes/*.json
 */
public class ClassManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "classes";

    private final Map<ResourceLocation, PlayerClass> classes = new HashMap<>();

    public ClassManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        classes.clear();

        jsonMap.forEach((id, json) -> {
            try {
                PlayerClass playerClass = PlayerClass.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> AetheriusMod.LOGGER.error("Failed to parse class {}: {}", id, error))
                    .orElse(null);

                if (playerClass != null) {
                    classes.put(id, playerClass);
                    AetheriusMod.LOGGER.info("Loaded class: {}", id);
                }
            } catch (Exception e) {
                AetheriusMod.LOGGER.error("Error loading class {}: {}", id, e.getMessage());
            }
        });

        AetheriusMod.LOGGER.info("Loaded {} classes", classes.size());
    }

    /**
     * Get a class by ID.
     */
    public Optional<PlayerClass> getPlayerClass(ResourceLocation id) {
        return Optional.ofNullable(classes.get(id));
    }

    /**
     * Get a class by string ID.
     */
    public Optional<PlayerClass> getPlayerClass(String idString) {
        return getPlayerClass(new ResourceLocation(idString));
    }

    /**
     * Get all loaded classes.
     */
    public Map<ResourceLocation, PlayerClass> getAllClasses() {
        return Map.copyOf(classes);
    }
}
