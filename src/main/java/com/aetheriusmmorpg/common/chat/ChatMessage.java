package com.aetheriusmmorpg.common.chat;

import java.util.UUID;

/**
 * Represents a chat message in the system.
 */
public class ChatMessage {

    private final UUID senderId;
    private final String senderName;
    private final ChatChannel channel;
    private final String message;
    private final long timestamp;
    private final UUID recipientId; // For PM messages

    public ChatMessage(UUID senderId, String senderName, ChatChannel channel, String message, UUID recipientId) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.channel = channel;
        this.message = message;
        this.recipientId = recipientId;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(UUID senderId, String senderName, ChatChannel channel, String message) {
        this(senderId, senderName, channel, message, null);
    }

    public String getFormattedMessage() {
        return channel.formatMessage(senderName, message);
    }

    // Getters
    public UUID getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public ChatChannel getChannel() { return channel; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public UUID getRecipientId() { return recipientId; }
    public boolean isPrivateMessage() { return recipientId != null; }
}
