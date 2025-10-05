package com.aetheriusmmorpg.network.packet.quest;

import com.aetheriusmmorpg.client.ClientQuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server->Client packet when a quest is completed.
 */
public record S2CQuestCompletePacket(ResourceLocation questId) {

    public S2CQuestCompletePacket(FriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(questId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientQuestData.completeQuest(questId);
        });
        ctx.get().setPacketHandled(true);
    }
}


