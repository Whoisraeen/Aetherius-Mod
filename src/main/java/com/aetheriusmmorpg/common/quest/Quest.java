package com.aetheriusmmorpg.common.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Represents a quest in Aetherius.
 * Loaded from datapack JSON files.
 */
public record Quest(
    ResourceLocation id,
    String name,
    String description,
    QuestType type,                    // MAIN, SIDE, DYNAMIC, DAILY
    int requiredLevel,
    ResourceLocation prerequisiteQuest, // Quest that must be completed first (optional)
    List<QuestObjective> objectives,
    QuestRewards rewards,
    String questGiver,                 // NPC ID who gives this quest
    boolean repeatable,
    int timeLimit                      // Time limit in seconds (0 = no limit)
) {

    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Quest::id),
            Codec.STRING.fieldOf("name").forGetter(Quest::name),
            Codec.STRING.fieldOf("description").forGetter(Quest::description),
            QuestType.CODEC.fieldOf("type").forGetter(Quest::type),
            Codec.INT.fieldOf("required_level").forGetter(Quest::requiredLevel),
            ResourceLocation.CODEC.optionalFieldOf("prerequisite_quest", null).forGetter(q -> q.prerequisiteQuest),
            QuestObjective.CODEC.listOf().fieldOf("objectives").forGetter(Quest::objectives),
            QuestRewards.CODEC.fieldOf("rewards").forGetter(Quest::rewards),
            Codec.STRING.fieldOf("quest_giver").forGetter(Quest::questGiver),
            Codec.BOOL.optionalFieldOf("repeatable", false).forGetter(Quest::repeatable),
            Codec.INT.optionalFieldOf("time_limit", 0).forGetter(Quest::timeLimit)
        ).apply(instance, Quest::new)
    );

    public enum QuestType {
        MAIN,      // Main storyline quests
        SIDE,      // Optional side quests
        DYNAMIC,   // Randomly generated quests
        DAILY,     // Daily repeatable quests
        GUILD;     // Guild-specific quests

        public static final Codec<QuestType> CODEC = Codec.STRING.xmap(
            str -> QuestType.valueOf(str.toUpperCase()),
            QuestType::name
        );
    }

    /**
     * Check if player meets requirements to accept this quest.
     */
    public boolean canAccept(int playerLevel, List<ResourceLocation> completedQuests) {
        if (playerLevel < requiredLevel) {
            return false;
        }

        if (prerequisiteQuest != null && !completedQuests.contains(prerequisiteQuest)) {
            return false;
        }

        return true;
    }

    /**
     * Get quest display component with formatting.
     */
    public Component getDisplayName() {
        String prefix = switch (type) {
            case MAIN -> "§6[Main] §f";
            case SIDE -> "§b[Side] §f";
            case DAILY -> "§a[Daily] §f";
            case DYNAMIC -> "§e[Dynamic] §f";
            case GUILD -> "§5[Guild] §f";
        };
        return Component.literal(prefix + name);
    }
}
