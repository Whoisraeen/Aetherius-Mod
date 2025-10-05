package com.aetheriusmmorpg.common.quest;

import com.aetheriusmmorpg.AetheriusMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages loading and accessing quests from datapacks.
 * Quests are loaded from data/[namespace]/quests/[quest_id].json
 */
public class QuestManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, Quest> QUESTS = new HashMap<>();

    public QuestManager() {
        super(GSON, "quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        QUESTS.clear();

        objects.forEach((id, json) -> {
            try {
                Quest.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> AetheriusMod.LOGGER.error("Failed to parse quest {}: {}", id, error))
                    .ifPresent(quest -> {
                        QUESTS.put(id, quest);
                        AetheriusMod.LOGGER.debug("Loaded quest: {}", id);
                    });
            } catch (Exception e) {
                AetheriusMod.LOGGER.error("Error loading quest {}", id, e);
            }
        });

        AetheriusMod.LOGGER.info("Loaded {} quests", QUESTS.size());
    }

    /**
     * Get a quest by ID.
     */
    public static Quest getQuest(ResourceLocation id) {
        return QUESTS.get(id);
    }

    /**
     * Get all loaded quests.
     */
    public static Collection<Quest> getAllQuests() {
        return QUESTS.values();
    }

    /**
     * Get quests available for a player based on level and prerequisites.
     */
    public static List<Quest> getAvailableQuests(int playerLevel, List<ResourceLocation> completedQuests) {
        return QUESTS.values().stream()
            .filter(quest -> quest.canAccept(playerLevel, completedQuests))
            .collect(Collectors.toList());
    }

    /**
     * Get quests by type.
     */
    public static List<Quest> getQuestsByType(Quest.QuestType type) {
        return QUESTS.values().stream()
            .filter(quest -> quest.type() == type)
            .collect(Collectors.toList());
    }

    /**
     * Get quests given by a specific NPC.
     */
    public static List<Quest> getQuestsByGiver(String npcId) {
        return QUESTS.values().stream()
            .filter(quest -> quest.questGiver().equals(npcId))
            .collect(Collectors.toList());
    }

    /**
     * Check if a quest exists.
     */
    public static boolean questExists(ResourceLocation id) {
        return QUESTS.containsKey(id);
    }
}
