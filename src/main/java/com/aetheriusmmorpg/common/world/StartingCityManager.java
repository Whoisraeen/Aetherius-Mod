package com.aetheriusmmorpg.common.world;

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
 * Manages loading and accessing starting cities from datapacks.
 * Cities are defined in data/<namespace>/starting_cities/*.json
 */
public class StartingCityManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "starting_cities";

    private final Map<ResourceLocation, StartingCity> cities = new HashMap<>();

    public StartingCityManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        cities.clear();

        jsonMap.forEach((id, json) -> {
            try {
                StartingCity city = StartingCity.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> AetheriusMod.LOGGER.error("Failed to parse starting city {}: {}", id, error))
                    .orElse(null);

                if (city != null) {
                    cities.put(id, city);
                    AetheriusMod.LOGGER.info("Loaded starting city: {}", id);
                }
            } catch (Exception e) {
                AetheriusMod.LOGGER.error("Error loading starting city {}: {}", id, e.getMessage());
            }
        });

        AetheriusMod.LOGGER.info("Loaded {} starting cities", cities.size());
    }

    /**
     * Get a starting city by ID.
     */
    public Optional<StartingCity> getCity(ResourceLocation id) {
        return Optional.ofNullable(cities.get(id));
    }

    /**
     * Get all cities.
     */
    public Map<ResourceLocation, StartingCity> getAllCities() {
        return new HashMap<>(cities);
    }
}

