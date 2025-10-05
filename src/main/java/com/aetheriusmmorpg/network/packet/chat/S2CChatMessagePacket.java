package com.aetheriusmmorpg.network.packet.chat;

import com.aetheriusmmorpg.client.ClientChatData;
import com.aetheriusmmorpg.common.chat.ChatChannel;
import com.aetheriusmmorpg.common.chat.ChatMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server->Client packet to deliver a chat message.
 */
public record S2CChatMessagePacket(
    UUID senderId,
    String senderName,
    ChatChannel channel,
    String message,
    long timestamp,
    UUID recipientId
) {

    public S2CChatMessagePacket(ChatMessage chatMessage) {
        this(
            chatMessage.getSenderId(),
            chatMessage.getSenderName(),
            chatMessage.getChannel(),
            chatMessage.getMessage(),
            chatMessage.getTimestamp(),
            chatMessage.getRecipientId()
        );
    }

    public S2CChatMessagePacket(FriendlyByteBuf buf) {
        this(
            buf.readUUID(),
            buf.readUtf(256),
            buf.readEnum(ChatChannel.class),
            buf.readUtf(512),
            buf.readLong(),
            buf.readBoolean() ? buf.readUUID() : null
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(senderId);
        buf.writeUtf(senderName, 256);
        buf.writeEnum(channel);
        buf.writeUtf(message, 512);
        buf.writeLong(timestamp);
        buf.writeBoolean(recipientId != null);
        if (recipientId != null) {
            buf.writeUUID(recipientId);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ChatMessage chatMessage = new ChatMessage(
                senderId,
                senderName,
                channel,
                message,
                recipientId
            );
            ClientChatData.addMessage(chatMessage);

            // Notify the advanced chat HUD
            com.aetheriusmmorpg.client.ui.AdvancedChatHUD.onMessageReceived();

            // Show notification for important messages
            com.aetheriusmmorpg.client.ui.ChatNotificationOverlay.addNotification(chatMessage);
        });
        ctx.get().setPacketHandled(true);
    }
}
