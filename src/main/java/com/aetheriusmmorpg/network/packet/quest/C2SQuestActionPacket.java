package com.aetheriusmmorpg.network.packet.quest;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import com.aetheriusmmorpg.common.quest.Quest;
import com.aetheriusmmorpg.common.quest.QuestManager;
import com.aetheriusmmorpg.common.quest.QuestProgress;
import com.aetheriusmmorpg.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client->Server packet for quest actions (accept, complete, abandon).
 */
public record C2SQuestActionPacket(
    QuestAction action,
    ResourceLocation questId
) {

    public C2SQuestActionPacket(FriendlyByteBuf buf) {
        this(
            buf.readEnum(QuestAction.class),
            buf.readResourceLocation()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeResourceLocation(questId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerRpgData.CAPABILITY).ifPresent(data -> {
                Quest quest = QuestManager.getQuest(questId);
                if (quest == null) {
                    AetheriusMod.LOGGER.warn("Player {} tried to interact with non-existent quest: {}", 
                        player.getName().getString(), questId);
                    return;
                }

                switch (action) {
                    case ACCEPT:
                        acceptQuest(player, data, quest);
                        break;
                    case COMPLETE:
                        completeQuest(player, data, quest);
                        break;
                    case ABANDON:
                        abandonQuest(player, data, quest);
                        break;
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private static void acceptQuest(ServerPlayer player, PlayerRpgData data, Quest quest) {
        // Check if player can accept quest
        if (!quest.canAccept(data.getLevel(), data.getCompletedQuests())) {
            player.sendSystemMessage(Component.literal("§cYou don't meet the requirements for this quest!"));
            return;
        }

        // Check if already has quest
        if (data.hasActiveQuest(quest.id())) {
            player.sendSystemMessage(Component.literal("§cYou already have this quest!"));
            return;
        }

        // Check if already completed (and not repeatable)
        if (data.hasCompletedQuest(quest.id()) && !quest.repeatable()) {
            player.sendSystemMessage(Component.literal("§cYou have already completed this quest!"));
            return;
        }

        // Accept quest
        QuestProgress progress = new QuestProgress(quest.id());
        data.addQuest(quest.id(), progress);

        player.sendSystemMessage(Component.literal("§a§lQUEST ACCEPTED!"));
        player.sendSystemMessage(Component.literal("§e" + quest.name()));
        
        // Sync quest data to client
        NetworkHandler.sendToPlayer(new S2CQuestUpdatePacket(quest.id(), progress), player);
        
        AetheriusMod.LOGGER.info("Player {} accepted quest: {}", player.getName().getString(), quest.name());
    }

    private static void completeQuest(ServerPlayer player, PlayerRpgData data, Quest quest) {
        QuestProgress progress = data.getQuestProgress(quest.id());
        
        if (progress == null) {
            player.sendSystemMessage(Component.literal("§cYou don't have this quest!"));
            return;
        }

        if (!progress.isComplete(quest)) {
            player.sendSystemMessage(Component.literal("§cYou haven't completed all objectives yet!"));
            return;
        }

        // Award rewards
        if (quest.rewards() != null) {
            // Experience
            data.addExperience(quest.rewards().experience());
            
            // Gold
            data.addGold(quest.rewards().gold());
            
            // Items
            quest.rewards().items().forEach(itemStack -> {
                if (!player.getInventory().add(itemStack.copy())) {
                    player.drop(itemStack.copy(), false);
                }
            });
            
            // Skills
            quest.rewards().skillUnlocks().forEach(data::unlockSkill);
        }

        // Complete quest
        data.completeQuest(quest.id());

        player.sendSystemMessage(Component.literal("§a§lQUEST COMPLETED!"));
        player.sendSystemMessage(Component.literal("§e" + quest.name()));
        if (quest.rewards() != null) {
            player.sendSystemMessage(Component.literal("§6Rewards: §f" + 
                quest.rewards().experience() + " XP, " + 
                quest.rewards().gold() + " Gold"));
        }
        
        // Sync data to client
        NetworkHandler.sendToPlayer(new S2CQuestCompletePacket(quest.id()), player);
        
        AetheriusMod.LOGGER.info("Player {} completed quest: {}", player.getName().getString(), quest.name());
    }

    private static void abandonQuest(ServerPlayer player, PlayerRpgData data, Quest quest) {
        if (!data.hasActiveQuest(quest.id())) {
            player.sendSystemMessage(Component.literal("§cYou don't have this quest!"));
            return;
        }

        data.removeQuest(quest.id());
        player.sendSystemMessage(Component.literal("§7Quest abandoned: " + quest.name()));
        
        // Sync to client
        NetworkHandler.sendToPlayer(new S2CQuestAbandonPacket(quest.id()), player);
        
        AetheriusMod.LOGGER.info("Player {} abandoned quest: {}", player.getName().getString(), quest.name());
    }

    public enum QuestAction {
        ACCEPT,
        COMPLETE,
        ABANDON
    }

    // Convenience constructors
    public static C2SQuestActionPacket accept(ResourceLocation questId) {
        return new C2SQuestActionPacket(QuestAction.ACCEPT, questId);
    }

    public static C2SQuestActionPacket complete(ResourceLocation questId) {
        return new C2SQuestActionPacket(QuestAction.COMPLETE, questId);
    }

    public static C2SQuestActionPacket abandon(ResourceLocation questId) {
        return new C2SQuestActionPacket(QuestAction.ABANDON, questId);
    }
}


