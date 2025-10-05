package com.aetheriusmmorpg.client.ui;

import com.aetheriusmmorpg.client.ClientChatData;
import com.aetheriusmmorpg.common.chat.ChatChannel;
import com.aetheriusmmorpg.common.chat.ChatMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Floating notification overlay for new chat messages.
 * Shows brief notifications in the top-right corner for important messages.
 */
public class ChatNotificationOverlay {

    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 50;
    private static final int NOTIFICATION_PADDING = 10;
    private static final int DISPLAY_TIME = 4000; // 4 seconds
    private static final int FADE_TIME = 500; // 0.5 seconds

    private static final List<Notification> activeNotifications = new ArrayList<>();

    public static class Notification {
        public final ChatMessage message;
        public final long timestamp;
        public float slideProgress = 0.0f;

        public Notification(ChatMessage message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > DISPLAY_TIME + FADE_TIME;
        }

        public float getAlpha() {
            long age = System.currentTimeMillis() - timestamp;

            // Fade in
            if (age < FADE_TIME) {
                return age / (float) FADE_TIME;
            }

            // Fade out
            if (age > DISPLAY_TIME) {
                return 1.0f - ((age - DISPLAY_TIME) / (float) FADE_TIME);
            }

            return 1.0f;
        }
    }

    /**
     * Add a notification for important messages.
     */
    public static void addNotification(ChatMessage message) {
        // Only show notifications for certain channels
        ChatChannel channel = message.getChannel();
        if (channel == ChatChannel.PM ||
            channel == ChatChannel.PARTY ||
            channel == ChatChannel.GUILD ||
            channel == ChatChannel.WORLD_EVENT) {

            activeNotifications.add(new Notification(message));

            // Limit to 3 notifications
            while (activeNotifications.size() > 3) {
                activeNotifications.remove(0);
            }
        }
    }

    /**
     * Render notifications.
     */
    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        // Remove expired notifications
        activeNotifications.removeIf(Notification::isExpired);

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int yOffset = NOTIFICATION_PADDING;

        for (Notification notification : activeNotifications) {
            // Update slide animation
            notification.slideProgress = Math.min(1.0f, notification.slideProgress + 0.1f);

            int x = screenWidth - NOTIFICATION_WIDTH - NOTIFICATION_PADDING;
            int slideX = (int) (x + (1.0f - notification.slideProgress) * NOTIFICATION_WIDTH);
            int y = yOffset;

            float alpha = notification.getAlpha();

            // Background
            int bgColor = (int) (alpha * 220) << 24 | 0x1A1A2E;
            renderGradientBox(guiGraphics, slideX, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, bgColor, bgColor & 0x80FFFFFF);

            // Border
            int borderColor = (int) (alpha * 255) << 24 | notification.message.getChannel().getColorCode().hashCode() | 0x6B4DB8;
            guiGraphics.fill(slideX, y, slideX + 2, y + NOTIFICATION_HEIGHT, borderColor);

            // Icon/Channel indicator
            String channelIcon = notification.message.getChannel().getDisplayName().substring(0, 1);
            int iconColor = (int) (alpha * 255) << 24 | 0xFFFFFF;
            guiGraphics.drawString(font, Component.literal("§l" + channelIcon), slideX + 10, y + 8, iconColor, true);

            // Sender name
            String sender = notification.message.getSenderName();
            if (sender.length() > 15) {
                sender = sender.substring(0, 15) + "...";
            }
            int textColor = (int) (alpha * 255) << 24 | 0xFFFFFF;
            guiGraphics.drawString(font, Component.literal("§b" + sender), slideX + 30, y + 8, textColor, true);

            // Message
            String msg = notification.message.getMessage();
            if (msg.length() > 35) {
                msg = msg.substring(0, 35) + "...";
            }
            guiGraphics.drawString(font, Component.literal("§7" + msg), slideX + 30, y + 22, textColor, true);

            yOffset += NOTIFICATION_HEIGHT + 5;
        }
    }

    private static void renderGradientBox(GuiGraphics guiGraphics, int x, int y, int width, int height, int colorTop, int colorBottom) {
        guiGraphics.fillGradient(x, y, x + width, y + height, colorTop, colorBottom);
    }
}
