package com.aetheriusmmorpg.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-side cache of quest data received from server.
 */
public class ClientQuestData {

    // Quest ID -> Objective Progress (array of current/required)
    private static final Map<ResourceLocation, int[]> activeQuests = new HashMap<>();
    private static final Set<ResourceLocation> completedQuests = new HashSet<>();
    private static ResourceLocation trackedQuest = null;

    public static void setTrackedQuest(ResourceLocation questId) {
        trackedQuest = questId;
    }

    public static ResourceLocation getTrackedQuest() {
        return trackedQuest;
    }

    public static void updateQuestProgress(ResourceLocation questId, int[] progress) {
        activeQuests.put(questId, progress);
    }

    public static void completeQuest(ResourceLocation questId) {
        activeQuests.remove(questId);
        completedQuests.add(questId);
    }

    public static void abandonQuest(ResourceLocation questId) {
        activeQuests.remove(questId);
    }

    public static void clearAllQuests() {
        activeQuests.clear();
        completedQuests.clear();
    }

    // Getters
    public static Map<ResourceLocation, int[]> getActiveQuests() {
        return new HashMap<>(activeQuests);
    }

    public static Set<ResourceLocation> getCompletedQuests() {
        return new HashSet<>(completedQuests);
    }

    public static int[] getQuestProgress(ResourceLocation questId) {
        return activeQuests.get(questId);
    }

    public static boolean hasActiveQuest(ResourceLocation questId) {
        return activeQuests.containsKey(questId);
    }

    public static boolean hasCompletedQuest(ResourceLocation questId) {
        return completedQuests.contains(questId);
    }
}


