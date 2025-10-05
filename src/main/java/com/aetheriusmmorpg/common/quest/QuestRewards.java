package com.aetheriusmmorpg.common.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * Represents rewards given upon quest completion.
 */
public record QuestRewards(
    long experience,
    long gold,
    List<ItemReward> items,
    Map<ResourceLocation, Integer> skills  // Skill ID -> Unlock at level
) {

    public static final Codec<QuestRewards> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.optionalFieldOf("experience", 0L).forGetter(QuestRewards::experience),
            Codec.LONG.optionalFieldOf("gold", 0L).forGetter(QuestRewards::gold),
            ItemReward.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(QuestRewards::items),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                .optionalFieldOf("skills", Map.of()).forGetter(QuestRewards::skills)
        ).apply(instance, QuestRewards::new)
    );

    /**
     * Represents an item reward with quantity.
     */
    public record ItemReward(
        ResourceLocation itemId,
        int quantity
    ) {
        public static final Codec<ItemReward> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(ItemReward::itemId),
                Codec.INT.fieldOf("quantity").forGetter(ItemReward::quantity)
            ).apply(instance, ItemReward::new)
        );
    }
}
