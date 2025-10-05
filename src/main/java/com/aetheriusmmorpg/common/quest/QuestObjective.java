package com.aetheriusmmorpg.common.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single objective within a quest.
 * Can be Kill, Collect, Interact, Discover, etc.
 */
public record QuestObjective(
    ObjectiveType type,
    ResourceLocation target,    // Entity ID, Item ID, Block ID, or Location ID
    int requiredAmount,
    String description
) {

    public static final Codec<QuestObjective> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ObjectiveType.CODEC.fieldOf("type").forGetter(QuestObjective::type),
            ResourceLocation.CODEC.fieldOf("target").forGetter(QuestObjective::target),
            Codec.INT.fieldOf("required_amount").forGetter(QuestObjective::requiredAmount),
            Codec.STRING.fieldOf("description").forGetter(QuestObjective::description)
        ).apply(instance, QuestObjective::new)
    );

    public enum ObjectiveType {
        KILL,           // Kill X amount of specific mob
        COLLECT,        // Collect X amount of specific item
        INTERACT,       // Interact with NPC or object
        DISCOVER,       // Discover a location
        CRAFT,          // Craft X amount of specific item
        USE_SKILL,      // Use a specific skill X times
        REACH_LEVEL,    // Reach a certain level
        COMPLETE_DUNGEON, // Complete a dungeon
        ESCORT,         // Escort NPC to location
        DEFEND;         // Defend location/NPC for duration

        public static final Codec<ObjectiveType> CODEC = Codec.STRING.xmap(
            str -> ObjectiveType.valueOf(str.toUpperCase()),
            ObjectiveType::name
        );
    }

    /**
     * Get formatted objective text with progress.
     */
    public String getProgressText(int current) {
        return description + " (" + Math.min(current, requiredAmount) + "/" + requiredAmount + ")";
    }

    /**
     * Check if objective is complete.
     */
    public boolean isComplete(int currentProgress) {
        return currentProgress >= requiredAmount;
    }
}
