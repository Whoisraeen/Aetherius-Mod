package com.aetheriusmmorpg.client.ui;

import com.aetheriusmmorpg.client.ClientChatData;
import com.aetheriusmmorpg.common.chat.ChatChannel;
import com.aetheriusmmorpg.common.chat.ChatMessage;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.chat.C2SChatMessagePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PWI-style advanced chat HUD with custom graphics.
 * Replaces Minecraft's default chat with a polished, modern interface.
 */
public class AdvancedChatHUD {

    private static final int CHAT_WIDTH = 420;
    private static final int CHAT_HEIGHT = 200;
    private static final int TAB_BAR_HEIGHT = 28;
    private static final int INPUT_HEIGHT = 24;
    private static final int MESSAGE_LINE_HEIGHT = 14;
    private static final int PADDING = 8;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private static boolean chatOpen = false;
    private static EditBox inputBox = null;
    private static ChatChannel activeChannel = ChatChannel.LOCAL;
    private static ChatChannel inputChannel = ChatChannel.LOCAL; // Channel for sending messages
    private static int scrollOffset = 0;
    private static long lastMessageTime = 0;
    private static final int FADE_DELAY = 5000; // 5 seconds before fade
    private static final int FADE_DURATION = 2000; // 2 seconds fade
    private static boolean channelDropdownOpen = false;
    private static final int CHANNEL_BUTTON_WIDTH = 100;
    private static final int CHANNEL_DROPDOWN_HEIGHT = 150;

    // Tab selection
    private static final ChatChannel[] VISIBLE_TABS = {
        ChatChannel.LOCAL,
        ChatChannel.GLOBAL,
        ChatChannel.PARTY,
        ChatChannel.GUILD,
        ChatChannel.TRADE,
        ChatChannel.PM
    };

    // All sendable channels for dropdown
    private static final ChatChannel[] SENDABLE_CHANNELS = {
        ChatChannel.LOCAL,
        ChatChannel.GLOBAL,
        ChatChannel.PARTY,
        ChatChannel.GUILD,
        ChatChannel.TRADE
    };

    /**
     * Open the chat input.
     */
    public static void open() {
        chatOpen = true;
        Minecraft mc = Minecraft.getInstance();

        if (inputBox == null) {
            inputBox = new EditBox(mc.font, 0, 0, CHAT_WIDTH - PADDING * 2, INPUT_HEIGHT - 4, Component.literal(""));
            inputBox.setMaxLength(512);
            inputBox.setBordered(false);
        }

        inputBox.setValue("");
        inputBox.setFocused(true);
        scrollOffset = 0;
    }

    /**
     * Close the chat input.
     */
    public static void close() {
        chatOpen = false;
        channelDropdownOpen = false;
        if (inputBox != null) {
            inputBox.setFocused(false);
        }
    }

    /**
     * Check if chat is open.
     */
    public static boolean isOpen() {
        return chatOpen;
    }

    /**
     * Send the current message.
     */
    public static void sendMessage() {
        if (inputBox == null || inputBox.getValue().trim().isEmpty()) {
            close();
            return;
        }

        String message = inputBox.getValue().trim();

        // Send to the selected input channel
        NetworkHandler.sendToServer(new C2SChatMessagePacket(inputChannel, message, null));

        inputBox.setValue("");
        close();
    }

    /**
     * Main render method.
     */
    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int x = PADDING;
        int y = screenHeight - CHAT_HEIGHT - PADDING;

        // Calculate fade based on time since last message
        float alpha = calculateAlpha();

        if (chatOpen) {
            alpha = 1.0f; // Full opacity when open
            renderFullChat(guiGraphics, mc, x, y, screenWidth, screenHeight);
        } else if (alpha > 0.0f) {
            renderCompactChat(guiGraphics, mc, x, y, screenWidth, screenHeight, alpha);
        }
    }

    /**
     * Render full chat window (when open).
     */
    private static void renderFullChat(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int screenWidth, int screenHeight) {
        // Main background with gradient
        renderGradientBox(guiGraphics, x, y, CHAT_WIDTH, CHAT_HEIGHT + TAB_BAR_HEIGHT + INPUT_HEIGHT + PADDING,
            0xE0000000, 0xD0000000);

        // Tab bar background
        renderGradientBox(guiGraphics, x, y, CHAT_WIDTH, TAB_BAR_HEIGHT, 0xFF1A1A2E, 0xFF16213E);

        // Render tabs
        renderTabs(guiGraphics, mc, x, y);

        // Messages area background
        int msgY = y + TAB_BAR_HEIGHT;
        renderGradientBox(guiGraphics, x + 2, msgY + 2, CHAT_WIDTH - 4, CHAT_HEIGHT - 4, 0xC0000000, 0xB0000000);

        // Render messages
        renderMessages(guiGraphics, mc, x + PADDING, msgY + PADDING, CHAT_WIDTH - PADDING * 2, CHAT_HEIGHT - PADDING * 2, 1.0f);

        // Render scrollbar if needed
        renderScrollbar(guiGraphics, x + CHAT_WIDTH - 16, msgY + PADDING, CHAT_HEIGHT - PADDING * 2);

        // Input box background
        int inputY = y + TAB_BAR_HEIGHT + CHAT_HEIGHT;
        renderGradientBox(guiGraphics, x + 4, inputY + 4, CHAT_WIDTH - 8, INPUT_HEIGHT, 0xFF0F3460, 0xFF16213E);

        // Channel selector button (PWI-style dropdown)
        renderChannelSelector(guiGraphics, mc, x + PADDING, inputY + 6);

        // Input box
        if (inputBox != null) {
            inputBox.setX(x + PADDING + CHANNEL_BUTTON_WIDTH + 8);
            inputBox.setY(inputY + 7);
            inputBox.setWidth(CHAT_WIDTH - PADDING * 2 - CHANNEL_BUTTON_WIDTH - 8);
            inputBox.render(guiGraphics, 0, 0, 0);
        }

        // Render channel dropdown if open (render last so it's on top)
        if (channelDropdownOpen) {
            renderChannelDropdown(guiGraphics, mc, x + PADDING, inputY + 6);
        }

        // Border glow effect
        renderBorder(guiGraphics, x, y, CHAT_WIDTH, CHAT_HEIGHT + TAB_BAR_HEIGHT + INPUT_HEIGHT + PADDING, 0xFF6B4DB8);
    }

    /**
     * Render compact chat (when closed, with fade).
     */
    private static void renderCompactChat(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int screenWidth, int screenHeight, float alpha) {
        int compactHeight = 120;

        // Semi-transparent background with fade
        int bgAlpha = (int)(alpha * 200);
        int bgColor = (bgAlpha << 24) | 0x000000;
        renderGradientBox(guiGraphics, x, y + CHAT_HEIGHT - compactHeight, CHAT_WIDTH, compactHeight, bgColor, bgColor & 0x80000000);

        // Render recent messages with fade
        renderMessages(guiGraphics, mc, x + PADDING, y + CHAT_HEIGHT - compactHeight + PADDING, CHAT_WIDTH - PADDING * 2, compactHeight - PADDING * 2, alpha);
    }

    /**
     * Render channel tabs.
     */
    private static void renderTabs(GuiGraphics guiGraphics, Minecraft mc, int x, int y) {
        int tabWidth = CHAT_WIDTH / VISIBLE_TABS.length;

        for (int i = 0; i < VISIBLE_TABS.length; i++) {
            ChatChannel channel = VISIBLE_TABS[i];
            int tabX = x + i * tabWidth;
            boolean isActive = channel == activeChannel;

            // Tab background
            if (isActive) {
                renderGradientBox(guiGraphics, tabX, y, tabWidth, TAB_BAR_HEIGHT, 0xFF6B4DB8, 0xFF533A8E);
                // Active indicator line
                guiGraphics.fill(tabX, y + TAB_BAR_HEIGHT - 2, tabX + tabWidth, y + TAB_BAR_HEIGHT, 0xFFE94560);
            } else {
                renderGradientBox(guiGraphics, tabX, y, tabWidth, TAB_BAR_HEIGHT, 0x80000000, 0x60000000);
            }

            // Tab text
            String tabText = channel.getDisplayName();
            int textWidth = mc.font.width(tabText);
            int textX = tabX + (tabWidth - textWidth) / 2;
            int textY = y + (TAB_BAR_HEIGHT - mc.font.lineHeight) / 2;

            int textColor = isActive ? 0xFFFFFFFF : 0xFFAAAAAA;
            guiGraphics.drawString(mc.font, Component.literal(tabText), textX, textY, textColor, true);

            // Separator
            if (i < VISIBLE_TABS.length - 1) {
                guiGraphics.fill(tabX + tabWidth - 1, y + 4, tabX + tabWidth, y + TAB_BAR_HEIGHT - 4, 0x40FFFFFF);
            }
        }
    }

    /**
     * Render messages.
     */
    private static void renderMessages(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int width, int height, float alpha) {
        List<ChatMessage> messages = ClientChatData.getMessages(activeChannel);
        int maxLines = height / MESSAGE_LINE_HEIGHT;

        int startIndex = Math.max(0, messages.size() - maxLines - scrollOffset);
        int endIndex = Math.min(messages.size(), startIndex + maxLines);

        int currentY = y;
        for (int i = startIndex; i < endIndex; i++) {
            ChatMessage msg = messages.get(i);
            renderMessage(guiGraphics, mc, msg, x, currentY, width, alpha);
            currentY += MESSAGE_LINE_HEIGHT;
        }
    }

    /**
     * Render a single message with formatting.
     */
    private static void renderMessage(GuiGraphics guiGraphics, Minecraft mc, ChatMessage msg, int x, int y, int width, float alpha) {
        ClientChatData.ChatSettings settings = ClientChatData.getSettings(msg.getChannel());

        String formattedMsg = msg.getFormattedMessage();

        // Add timestamp if enabled
        if (settings.shouldShowTimestamps()) {
            String time = TIME_FORMAT.format(new Date(msg.getTimestamp()));
            formattedMsg = "§8[" + time + "] §r" + formattedMsg;
        }

        // Word wrap
        List<String> lines = wrapText(mc.font, formattedMsg, width);

        for (String line : lines) {
            int textColor = 0xFFFFFF | ((int)(alpha * 255) << 24);
            guiGraphics.drawString(mc.font, Component.literal(line), x, y, textColor, true);
            break; // Only show first line for now
        }
    }

    /**
     * Render scrollbar.
     */
    private static void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int height) {
        List<ChatMessage> messages = ClientChatData.getMessages(activeChannel);
        int maxLines = height / MESSAGE_LINE_HEIGHT;

        if (messages.size() <= maxLines) return;

        // Scrollbar track
        renderGradientBox(guiGraphics, x, y, 12, height, 0x80000000, 0x60000000);

        // Scrollbar thumb
        float thumbHeight = Math.max(20, height * ((float) maxLines / messages.size()));
        float maxScroll = messages.size() - maxLines;
        float thumbY = y + (height - thumbHeight) * (scrollOffset / maxScroll);

        renderGradientBox(guiGraphics, x + 2, (int)thumbY, 8, (int)thumbHeight, 0xFF6B4DB8, 0xFF533A8E);
    }

    /**
     * Render gradient box for modern look.
     */
    private static void renderGradientBox(GuiGraphics guiGraphics, int x, int y, int width, int height, int colorTop, int colorBottom) {
        guiGraphics.fillGradient(x, y, x + width, y + height, colorTop, colorBottom);
    }

    /**
     * Render border with glow effect.
     */
    private static void renderBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // Top
        guiGraphics.fill(x, y, x + width, y + 1, color);
        // Bottom
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        guiGraphics.fill(x, y, x + 1, y + height, color);
        // Right
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);

        // Outer glow
        int glowColor = (color & 0x00FFFFFF) | 0x40000000;
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, glowColor);
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, glowColor);
        guiGraphics.fill(x - 1, y, x, y + height, glowColor);
        guiGraphics.fill(x + width, y, x + width + 1, y + height, glowColor);
    }

    /**
     * Calculate fade alpha based on time since last message.
     */
    private static float calculateAlpha() {
        long timeSince = System.currentTimeMillis() - lastMessageTime;

        if (timeSince < FADE_DELAY) {
            return 1.0f; // Full opacity
        }

        long fadeTime = timeSince - FADE_DELAY;
        if (fadeTime >= FADE_DURATION) {
            return 0.0f; // Fully faded
        }

        return 1.0f - ((float)fadeTime / FADE_DURATION);
    }

    /**
     * Update last message time.
     */
    public static void onMessageReceived() {
        lastMessageTime = System.currentTimeMillis();
    }

    /**
     * Handle mouse click on tabs.
     */
    public static boolean mouseClicked(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        if (!chatOpen) return false;

        int x = PADDING;
        int y = screenHeight - CHAT_HEIGHT - PADDING;
        int tabWidth = CHAT_WIDTH / VISIBLE_TABS.length;

        // Check tab clicks
        if (mouseY >= y && mouseY < y + TAB_BAR_HEIGHT) {
            for (int i = 0; i < VISIBLE_TABS.length; i++) {
                int tabX = x + i * tabWidth;
                if (mouseX >= tabX && mouseX < tabX + tabWidth) {
                    activeChannel = VISIBLE_TABS[i];
                    ClientChatData.setActiveChannel(activeChannel);
                    scrollOffset = 0;
                    return true;
                }
            }
        }

        // Check channel selector button click
        int inputY = y + TAB_BAR_HEIGHT + CHAT_HEIGHT;
        int selectorX = x + PADDING;
        int selectorY = inputY + 6;

        if (mouseX >= selectorX && mouseX < selectorX + CHANNEL_BUTTON_WIDTH &&
            mouseY >= selectorY && mouseY < selectorY + INPUT_HEIGHT - 4) {
            // Toggle dropdown
            channelDropdownOpen = !channelDropdownOpen;
            return true;
        }

        // Check dropdown clicks (if open)
        if (channelDropdownOpen) {
            int dropdownX = selectorX;
            int dropdownY = selectorY - CHANNEL_DROPDOWN_HEIGHT - 2;

            if (mouseX >= dropdownX && mouseX < dropdownX + CHANNEL_BUTTON_WIDTH &&
                mouseY >= dropdownY && mouseY < dropdownY + CHANNEL_DROPDOWN_HEIGHT) {

                // Determine which channel was clicked
                int itemHeight = CHANNEL_DROPDOWN_HEIGHT / SENDABLE_CHANNELS.length;
                int clickedIndex = (int)((mouseY - dropdownY) / itemHeight);

                if (clickedIndex >= 0 && clickedIndex < SENDABLE_CHANNELS.length) {
                    inputChannel = SENDABLE_CHANNELS[clickedIndex];
                    channelDropdownOpen = false;
                    return true;
                }
            }

            // Click outside dropdown - close it
            channelDropdownOpen = false;
        }

        return false;
    }

    /**
     * Handle mouse scroll.
     */
    public static boolean mouseScrolled(double delta) {
        if (!chatOpen) return false;

        List<ChatMessage> messages = ClientChatData.getMessages(activeChannel);
        int maxLines = (CHAT_HEIGHT - PADDING * 2) / MESSAGE_LINE_HEIGHT;
        int maxScroll = Math.max(0, messages.size() - maxLines);

        if (delta > 0) {
            scrollOffset = Math.min(scrollOffset + 3, maxScroll);
        } else {
            scrollOffset = Math.max(scrollOffset - 3, 0);
        }

        return true;
    }

    /**
     * Handle key press.
     */
    public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!chatOpen) return false;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // If dropdown is open, close it first
            if (channelDropdownOpen) {
                channelDropdownOpen = false;
                return true;
            }
            // Otherwise close the chat
            close();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            // Close dropdown if open
            if (channelDropdownOpen) {
                channelDropdownOpen = false;
                return true;
            }
            sendMessage();
            return true;
        }

        if (inputBox != null) {
            return inputBox.keyPressed(keyCode, scanCode, modifiers);
        }

        return false;
    }

    /**
     * Handle character typed.
     */
    public static boolean charTyped(char character, int modifiers) {
        if (!chatOpen || inputBox == null) return false;
        return inputBox.charTyped(character, modifiers);
    }

    /**
     * Render the channel selector button (PWI-style dropdown).
     */
    private static void renderChannelSelector(GuiGraphics guiGraphics, Minecraft mc, int x, int y) {
        // Button background
        int buttonColor = channelDropdownOpen ? 0xFF533A8E : 0xFF0F3460;
        renderGradientBox(guiGraphics, x, y, CHANNEL_BUTTON_WIDTH, INPUT_HEIGHT - 4, buttonColor, buttonColor & 0xCC000000);

        // Channel text with color
        String channelText = inputChannel.getColorCode() + inputChannel.getDisplayName();
        int textX = x + 6;
        int textY = y + (INPUT_HEIGHT - 4 - mc.font.lineHeight) / 2;
        guiGraphics.drawString(mc.font, Component.literal(channelText), textX, textY, 0xFFFFFFFF, true);

        // Dropdown arrow
        String arrow = channelDropdownOpen ? "▲" : "▼";
        int arrowX = x + CHANNEL_BUTTON_WIDTH - mc.font.width(arrow) - 6;
        guiGraphics.drawString(mc.font, Component.literal("§7" + arrow), arrowX, textY, 0xFFFFFFFF, true);

        // Border
        int borderColor = channelDropdownOpen ? 0xFF6B4DB8 : 0x40FFFFFF;
        guiGraphics.fill(x, y, x + CHANNEL_BUTTON_WIDTH, y + 1, borderColor); // Top
        guiGraphics.fill(x, y + INPUT_HEIGHT - 4, x + CHANNEL_BUTTON_WIDTH, y + INPUT_HEIGHT - 3, borderColor); // Bottom
        guiGraphics.fill(x, y, x + 1, y + INPUT_HEIGHT - 4, borderColor); // Left
        guiGraphics.fill(x + CHANNEL_BUTTON_WIDTH - 1, y, x + CHANNEL_BUTTON_WIDTH, y + INPUT_HEIGHT - 4, borderColor); // Right
    }

    /**
     * Render the channel dropdown menu.
     */
    private static void renderChannelDropdown(GuiGraphics guiGraphics, Minecraft mc, int x, int y) {
        int dropdownX = x;
        int dropdownY = y - CHANNEL_DROPDOWN_HEIGHT - 2;

        // Background
        renderGradientBox(guiGraphics, dropdownX, dropdownY, CHANNEL_BUTTON_WIDTH, CHANNEL_DROPDOWN_HEIGHT, 0xF01A1A2E, 0xE016213E);

        // Border
        renderBorder(guiGraphics, dropdownX, dropdownY, CHANNEL_BUTTON_WIDTH, CHANNEL_DROPDOWN_HEIGHT, 0xFF6B4DB8);

        // Channel options
        int itemHeight = CHANNEL_DROPDOWN_HEIGHT / SENDABLE_CHANNELS.length;
        for (int i = 0; i < SENDABLE_CHANNELS.length; i++) {
            ChatChannel channel = SENDABLE_CHANNELS[i];
            int itemY = dropdownY + i * itemHeight;

            // Hover/selected background
            if (channel == inputChannel) {
                renderGradientBox(guiGraphics, dropdownX + 2, itemY + 2, CHANNEL_BUTTON_WIDTH - 4, itemHeight - 2, 0xFF6B4DB8, 0xFF533A8E);
            }

            // Channel name with color
            String channelText = channel.getColorCode() + channel.getDisplayName();
            int textY = itemY + (itemHeight - mc.font.lineHeight) / 2;
            guiGraphics.drawString(mc.font, Component.literal(channelText), dropdownX + 8, textY, 0xFFFFFFFF, true);
        }
    }

    /**
     * Word wrap helper.
     */
    private static List<String> wrapText(Font font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();

        if (font.width(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }

        // Simple wrapping (can be improved)
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (font.width(testLine) > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
