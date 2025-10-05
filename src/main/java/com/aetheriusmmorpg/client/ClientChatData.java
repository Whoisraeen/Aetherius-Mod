package com.aetheriusmmorpg.client;

import com.aetheriusmmorpg.common.chat.ChatChannel;
import com.aetheriusmmorpg.common.chat.ChatMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache for chat messages and settings.
 */
public class ClientChatData {

    private static final int MAX_MESSAGES_PER_CHANNEL = 200;

    // Messages by channel
    private static final Map<ChatChannel, List<ChatMessage>> channelMessages = new ConcurrentHashMap<>();

    // Chat settings
    private static ChatChannel activeChannel = ChatChannel.LOCAL;
    private static final Map<ChatChannel, ChatSettings> channelSettings = new ConcurrentHashMap<>();

    static {
        // Initialize message lists for each channel
        for (ChatChannel channel : ChatChannel.values()) {
            channelMessages.put(channel, Collections.synchronizedList(new ArrayList<>()));
            channelSettings.put(channel, new ChatSettings(channel));
        }
    }

    /**
     * Add a message to the appropriate channel.
     */
    public static void addMessage(ChatMessage message) {
        ChatChannel channel = message.getChannel();
        List<ChatMessage> messages = channelMessages.get(channel);

        if (messages != null) {
            messages.add(message);

            // Trim old messages if exceeding max
            if (messages.size() > MAX_MESSAGES_PER_CHANNEL) {
                messages.remove(0);
            }

            // Also add to ALL tab (except system messages)
            if (channel != ChatChannel.SYSTEM && channel != ChatChannel.WORLD_EVENT) {
                // For now, all messages are visible in their respective channels
            }
        }
    }

    /**
     * Get all messages for a specific channel.
     */
    public static List<ChatMessage> getMessages(ChatChannel channel) {
        return new ArrayList<>(channelMessages.getOrDefault(channel, Collections.emptyList()));
    }

    /**
     * Get recent messages (last N) for a channel.
     */
    public static List<ChatMessage> getRecentMessages(ChatChannel channel, int count) {
        List<ChatMessage> messages = channelMessages.get(channel);
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        int size = messages.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(start, size));
    }

    /**
     * Clear all messages for a specific channel.
     */
    public static void clearChannel(ChatChannel channel) {
        List<ChatMessage> messages = channelMessages.get(channel);
        if (messages != null) {
            messages.clear();
        }
    }

    /**
     * Clear all messages.
     */
    public static void clearAll() {
        for (List<ChatMessage> messages : channelMessages.values()) {
            messages.clear();
        }
    }

    /**
     * Get the currently active channel.
     */
    public static ChatChannel getActiveChannel() {
        return activeChannel;
    }

    /**
     * Set the active channel.
     */
    public static void setActiveChannel(ChatChannel channel) {
        activeChannel = channel;
    }

    /**
     * Get settings for a channel.
     */
    public static ChatSettings getSettings(ChatChannel channel) {
        return channelSettings.get(channel);
    }

    /**
     * Check if a channel is enabled.
     */
    public static boolean isChannelEnabled(ChatChannel channel) {
        ChatSettings settings = channelSettings.get(channel);
        return settings != null && settings.isEnabled();
    }

    /**
     * Chat settings for a specific channel.
     */
    public static class ChatSettings {
        private final ChatChannel channel;
        private boolean enabled = true;
        private boolean showTimestamps = false;
        private boolean playSound = true;
        private float opacity = 1.0f;

        public ChatSettings(ChatChannel channel) {
            this.channel = channel;

            // System and world event channels always enabled
            if (channel == ChatChannel.SYSTEM || channel == ChatChannel.WORLD_EVENT) {
                this.enabled = true;
            }
        }

        public ChatChannel getChannel() {
            return channel;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            // Can't disable system channels
            if (channel != ChatChannel.SYSTEM && channel != ChatChannel.WORLD_EVENT) {
                this.enabled = enabled;
            }
        }

        public boolean shouldShowTimestamps() {
            return showTimestamps;
        }

        public void setShowTimestamps(boolean showTimestamps) {
            this.showTimestamps = showTimestamps;
        }

        public boolean shouldPlaySound() {
            return playSound;
        }

        public void setPlaySound(boolean playSound) {
            this.playSound = playSound;
        }

        public float getOpacity() {
            return opacity;
        }

        public void setOpacity(float opacity) {
            this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        }
    }
}
