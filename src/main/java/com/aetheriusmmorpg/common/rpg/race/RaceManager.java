package com.aetheriusmmorpg.common.rpg.race;

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
 * Manages loading and accessing races from datapacks.
 * Races are defined in data/<namespace>/races/*.json
 */
public class RaceManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "races";

    private final Map<ResourceLocation, Race> races = new HashMap<>();

    public RaceManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        races.clear();

        jsonMap.forEach((id, json) -> {
            try {
                Race race = Race.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> AetheriusMod.LOGGER.error("Failed to parse race {}: {}", id, error))
                    .orElse(null);

                if (race != null) {
                    races.put(id, race);
                    AetheriusMod.LOGGER.info("Loaded race: {}", id);
                }
            } catch (Exception e) {
                AetheriusMod.LOGGER.error("Error loading race {}: {}", id, e.getMessage());
            }
        });

        AetheriusMod.LOGGER.info("Loaded {} races", races.size());
    }

    /**
     * Get a race by ID.
     */
    public Optional<Race> getRace(ResourceLocation id) {
        return Optional.ofNullable(races.get(id));
    }

    /**
     * Get a race by string ID.
     */
    public Optional<Race> getRace(String idString) {
        return getRace(new ResourceLocation(idString));
    }

    /**
     * Get all loaded races.
     */
    public Map<ResourceLocation, Race> getAllRaces() {
        return Map.copyOf(races);
    }
}
