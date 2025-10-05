package com.aetheriusmmorpg.network.packet.quest;

import com.aetheriusmmorpg.client.ClientQuestData;
import com.aetheriusmmorpg.common.quest.QuestProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server->Client packet to update quest progress.
 */
public record S2CQuestUpdatePacket(
    ResourceLocation questId,
    int[] objectiveProgress
) {

    public S2CQuestUpdatePacket(ResourceLocation questId, QuestProgress progress) {
        this(questId, progress.getObjectiveProgress());
    }

    public S2CQuestUpdatePacket(FriendlyByteBuf buf) {
        this(
            buf.readResourceLocation(),
            buf.readVarIntArray()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(questId);
        buf.writeVarIntArray(objectiveProgress);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientQuestData.updateQuestProgress(questId, objectiveProgress);
        });
        ctx.get().setPacketHandled(true);
    }
}


