package com.aetheriusmmorpg.network.packet.chat;

import com.aetheriusmmorpg.common.chat.ChatChannel;
import com.aetheriusmmorpg.common.chat.ChatManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client->Server packet to send a chat message.
 */
public record C2SChatMessagePacket(
    ChatChannel channel,
    String message,
    UUID recipientId // Only used for PM channel
) {

    public C2SChatMessagePacket(FriendlyByteBuf buf) {
        this(
            buf.readEnum(ChatChannel.class),
            buf.readUtf(512),
            buf.readBoolean() ? buf.readUUID() : null
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(channel);
        buf.writeUtf(message, 512);
        buf.writeBoolean(recipientId != null);
        if (recipientId != null) {
            buf.writeUUID(recipientId);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                ChatManager.sendMessage(sender, channel, message, recipientId);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // Convenience constructors
    public static C2SChatMessagePacket global(String message) {
        return new C2SChatMessagePacket(ChatChannel.GLOBAL, message, null);
    }

    public static C2SChatMessagePacket local(String message) {
        return new C2SChatMessagePacket(ChatChannel.LOCAL, message, null);
    }

    public static C2SChatMessagePacket guild(String message) {
        return new C2SChatMessagePacket(ChatChannel.GUILD, message, null);
    }

    public static C2SChatMessagePacket party(String message) {
        return new C2SChatMessagePacket(ChatChannel.PARTY, message, null);
    }

    public static C2SChatMessagePacket trade(String message) {
        return new C2SChatMessagePacket(ChatChannel.TRADE, message, null);
    }

    public static C2SChatMessagePacket pm(String message, UUID recipientId) {
        return new C2SChatMessagePacket(ChatChannel.PM, message, recipientId);
    }

    public static C2SChatMessagePacket faction(String message) {
        return new C2SChatMessagePacket(ChatChannel.FACTION, message, null);
    }
}
