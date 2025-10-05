package com.aetheriusmmorpg.common.rpg.skill;

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
 * Manages loading and accessing skills from datapacks.
 * Skills are defined in data/<namespace>/skills/*.json
 */
public class SkillManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "skills";

    private final Map<ResourceLocation, Skill> skills = new HashMap<>();

    public SkillManager() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        skills.clear();

        jsonMap.forEach((id, json) -> {
            try {
                Skill skill = Skill.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> AetheriusMod.LOGGER.error("Failed to parse skill {}: {}", id, error))
                    .orElse(null);

                if (skill != null) {
                    skills.put(id, skill);
                    AetheriusMod.LOGGER.info("Loaded skill: {}", id);
                }
            } catch (Exception e) {
                AetheriusMod.LOGGER.error("Error loading skill {}: {}", id, e.getMessage());
            }
        });

        AetheriusMod.LOGGER.info("Loaded {} skills", skills.size());
    }

    /**
     * Get a skill by ID.
     */
    public Optional<Skill> getSkill(ResourceLocation id) {
        return Optional.ofNullable(skills.get(id));
    }

    /**
     * Get a skill by string ID.
     */
    public Optional<Skill> getSkill(String idString) {
        return getSkill(new ResourceLocation(idString));
    }

    /**
     * Get all loaded skills.
     */
    public Map<ResourceLocation, Skill> getAllSkills() {
        return Map.copyOf(skills);
    }
}
