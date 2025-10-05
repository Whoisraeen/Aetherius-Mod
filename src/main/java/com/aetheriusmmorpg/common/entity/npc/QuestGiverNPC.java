package com.aetheriusmmorpg.common.entity.npc;

import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import com.aetheriusmmorpg.common.quest.Quest;
import com.aetheriusmmorpg.common.quest.QuestManager;
import com.aetheriusmmorpg.common.quest.QuestProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Quest Giver NPC that offers quests to players.
 * Can offer multiple quests and track quest completion.
 */
public class QuestGiverNPC extends AetheriusNPC {

    private final List<ResourceLocation> availableQuests = new ArrayList<>();
    private String greetingMessage = "Greetings, traveler! I have work for you.";
    private String questCompleteMessage = "Well done! Here is your reward.";
    private String noQuestsMessage = "I have no tasks for you at this time.";

    public QuestGiverNPC(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNPCType(NPCType.QUEST_GIVER);
    }

    /**
     * Add a quest that this NPC can offer.
     */
    public void addQuest(ResourceLocation questId) {
        if (!availableQuests.contains(questId)) {
            availableQuests.add(questId);
        }
    }

    /**
     * Remove a quest from this NPC's available quests.
     */
    public void removeQuest(ResourceLocation questId) {
        availableQuests.remove(questId);
    }

    /**
     * Get all quests this NPC can offer.
     */
    public List<ResourceLocation> getAvailableQuests() {
        return new ArrayList<>(availableQuests);
    }

    @Override
    protected void openQuestDialog(Player player) {
        player.getCapability(PlayerRpgData.CAPABILITY).ifPresent(data -> {
            // Check for completed quests first
            List<QuestProgress> activeQuests = data.getActiveQuests();
            boolean hasCompletedQuest = false;

            for (QuestProgress progress : activeQuests) {
                Quest quest = QuestManager.getQuest(progress.getQuestId());
                if (quest != null && quest.questGiver().equals(this.getNPCId())) {
                    // Check if all objectives are complete
                    if (progress.areAllObjectivesComplete(quest)) {
                        completeQuest(player, data, quest, progress);
                        hasCompletedQuest = true;
                        break; // Handle one quest at a time
                    }
                }
            }

            if (hasCompletedQuest) {
                return; // Quest completion dialog shown
            }

            // Check for new quests to offer
            List<Quest> offeredQuests = new ArrayList<>();
            for (ResourceLocation questId : availableQuests) {
                Quest quest = QuestManager.getQuest(questId);
                if (quest != null) {
                    // Check if player can accept this quest
                    if (quest.canAccept(data.getLevel(), data.getCompletedQuests())) {
                        // Check if not already active
                        if (data.getQuestProgress(questId) == null) {
                            offeredQuests.add(quest);
                        }
                    }
                }
            }

            if (offeredQuests.isEmpty()) {
                player.sendSystemMessage(Component.literal("§e" + getNpcDisplayName() + "§f: " + noQuestsMessage));
            } else {
                player.sendSystemMessage(Component.literal("§e" + getNpcDisplayName() + "§f: " + greetingMessage));

                // Show first available quest (in full implementation, would open GUI)
                Quest quest = offeredQuests.get(0);
                player.sendSystemMessage(Component.literal("§6[Quest Available] §f" + quest.name()));
                player.sendSystemMessage(Component.literal("§7" + quest.description()));

                // Auto-accept for testing (in full implementation, player chooses)
                acceptQuest(player, data, quest);
            }
        });
    }

    private void acceptQuest(Player player, PlayerRpgData data, Quest quest) {
        QuestProgress progress = new QuestProgress(quest.id());
        progress.setStatus(QuestProgress.QuestStatus.ACTIVE);
        data.setQuestProgress(progress);

        player.sendSystemMessage(Component.literal("§aQuest Accepted: §f" + quest.name()));
    }

    private void completeQuest(Player player, PlayerRpgData data, Quest quest, QuestProgress progress) {
        // Mark quest as complete
        data.completeQuest(quest.id());

        // Give rewards
        if (quest.rewards().experience() > 0) {
            data.addExperience(quest.rewards().experience());
            player.sendSystemMessage(Component.literal("§a+§f" + quest.rewards().experience() + " XP"));
        }

        if (quest.rewards().gold() > 0) {
            data.addGold(quest.rewards().gold());
            player.sendSystemMessage(Component.literal("§6+§f" + quest.rewards().gold() + " Gold"));
        }

        // Give item rewards (player is guaranteed to be ServerPlayer on server side)
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            quest.rewards().items().forEach(itemReward -> {
                com.aetheriusmmorpg.common.util.ItemRewardUtil.giveItems(serverPlayer, itemReward.itemId(), itemReward.quantity());
                player.sendSystemMessage(Component.literal("§b+§f" + itemReward.quantity() + "x " + itemReward.itemId()));
            });
        }

        // Unlock skills
        quest.rewards().skills().forEach((skillId, level) -> {
            // Add skill to player's unlocked skills
            data.unlockSkill(skillId);
            player.sendSystemMessage(Component.literal("§dUnlocked Skill: §f" + skillId));
        });

        player.sendSystemMessage(Component.literal("§e" + getNpcDisplayName() + "§f: " + questCompleteMessage));
        player.sendSystemMessage(Component.literal("§aQuest Completed: §f" + quest.name()));
    }

    // Custom messages
    public void setGreetingMessage(String message) {
        this.greetingMessage = message;
    }

    public void setQuestCompleteMessage(String message) {
        this.questCompleteMessage = message;
    }

    public void setNoQuestsMessage(String message) {
        this.noQuestsMessage = message;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        ListTag questsTag = new ListTag();
        for (ResourceLocation questId : availableQuests) {
            questsTag.add(StringTag.valueOf(questId.toString()));
        }
        tag.put("AvailableQuests", questsTag);

        tag.putString("GreetingMessage", greetingMessage);
        tag.putString("QuestCompleteMessage", questCompleteMessage);
        tag.putString("NoQuestsMessage", noQuestsMessage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        availableQuests.clear();
        if (tag.contains("AvailableQuests")) {
            ListTag questsTag = tag.getList("AvailableQuests", 8); // 8 = String type
            for (int i = 0; i < questsTag.size(); i++) {
                availableQuests.add(new ResourceLocation(questsTag.getString(i)));
            }
        }

        if (tag.contains("GreetingMessage")) {
            greetingMessage = tag.getString("GreetingMessage");
        }
        if (tag.contains("QuestCompleteMessage")) {
            questCompleteMessage = tag.getString("QuestCompleteMessage");
        }
        if (tag.contains("NoQuestsMessage")) {
            noQuestsMessage = tag.getString("NoQuestsMessage");
        }
    }
}
