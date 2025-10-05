package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientChatData;
import com.aetheriusmmorpg.common.chat.ChatChannel;
import com.aetheriusmmorpg.common.chat.ChatMessage;
import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.chat.C2SChatMessagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PWI-style multi-channel chat screen.
 * Features tabbed interface for different chat channels, scrollback, and channel switching.
 */
public class ChatScreen extends Screen {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;
    private static final int TAB_HEIGHT = 24;
    private static final int MESSAGE_LINE_HEIGHT = 12;
    private static final int MAX_VISIBLE_MESSAGES = 20;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private EditBox messageInput;
    private int windowX;
    private int windowY;
    private int scrollOffset = 0;
    private ChatChannel currentChannel;

    // Tab buttons
    private Button globalTab;
    private Button localTab;
    private Button guildTab;
    private Button partyTab;
    private Button tradeTab;
    private Button pmTab;

    public ChatScreen() {
        super(Component.literal("Chat"));
        this.currentChannel = ClientChatData.getActiveChannel();
    }

    @Override
    protected void init() {
        super.init();

        this.windowX = (this.width - WINDOW_WIDTH) / 2;
        this.windowY = (this.height - WINDOW_HEIGHT) / 2;

        // Create channel tabs
        int tabWidth = 90;
        int tabX = windowX + 5;
        int tabY = windowY + 5;

        globalTab = Button.builder(Component.literal("Global"),
            btn -> switchChannel(ChatChannel.GLOBAL))
            .bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
            .build();
        addRenderableWidget(globalTab);
        tabX += tabWidth + 2;

        localTab = Button.builder(Component.literal("Local"),
            btn -> switchChannel(ChatChannel.LOCAL))
            .bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
            .build();
        addRenderableWidget(localTab);
        tabX += tabWidth + 2;

        guildTab = Button.builder(Component.literal("Guild"),
            btn -> switchChannel(ChatChannel.GUILD))
            .bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
            .build();
        addRenderableWidget(guildTab);
        tabX += tabWidth + 2;

        partyTab = Button.builder(Component.literal("Party"),
            btn -> switchChannel(ChatChannel.PARTY))
            .bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
            .build();
        addRenderableWidget(partyTab);
        tabX += tabWidth + 2;

        tradeTab = Button.builder(Component.literal("Trade"),
            btn -> switchChannel(ChatChannel.TRADE))
            .bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
            .build();
        addRenderableWidget(tradeTab);
        tabX += tabWidth + 2;

        pmTab = Button.builder(Component.literal("PM"),
            btn -> switchChannel(ChatChannel.PM))
            .bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
            .build();
        addRenderableWidget(pmTab);

        // Message input box
        int inputY = windowY + WINDOW_HEIGHT - 30;
        messageInput = new EditBox(font, windowX + 10, inputY, WINDOW_WIDTH - 120, 20,
            Component.literal("Message"));
        messageInput.setMaxLength(512);
        messageInput.setHint(Component.literal("Type a message..."));
        messageInput.setFocused(true);
        addRenderableWidget(messageInput);

        // Send button
        Button sendButton = Button.builder(Component.literal("Send"),
            btn -> sendMessage())
            .bounds(windowX + WINDOW_WIDTH - 100, inputY, 90, 20)
            .build();
        addRenderableWidget(sendButton);
    }

    private void switchChannel(ChatChannel channel) {
        this.currentChannel = channel;
        ClientChatData.setActiveChannel(channel);
        this.scrollOffset = 0;
    }

    private void sendMessage() {
        String message = messageInput.getValue().trim();
        if (message.isEmpty()) {
            return;
        }

        // Parse channel prefix if present
        ChatChannel targetChannel = currentChannel;
        if (message.startsWith("/g ")) {
            targetChannel = ChatChannel.GLOBAL;
            message = message.substring(3);
        } else if (message.startsWith("/l ")) {
            targetChannel = ChatChannel.LOCAL;
            message = message.substring(3);
        } else if (message.startsWith("/gu ")) {
            targetChannel = ChatChannel.GUILD;
            message = message.substring(4);
        } else if (message.startsWith("/p ")) {
            targetChannel = ChatChannel.PARTY;
            message = message.substring(3);
        } else if (message.startsWith("/t ")) {
            targetChannel = ChatChannel.TRADE;
            message = message.substring(3);
        } else if (message.startsWith("/pm ")) {
            targetChannel = ChatChannel.PM;
            message = message.substring(4);
            // TODO: Parse recipient name from message
        }

        // Send packet to server
        C2SChatMessagePacket packet = new C2SChatMessagePacket(targetChannel, message, null);
        NetworkHandler.sendToServer(packet);

        // Clear input
        messageInput.setValue("");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Window background
        guiGraphics.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xE0000000);

        // Window border
        guiGraphics.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + 1, 0xFF6B4DB8); // Top
        guiGraphics.fill(windowX, windowY + WINDOW_HEIGHT - 1, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Bottom
        guiGraphics.fill(windowX, windowY, windowX + 1, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Left
        guiGraphics.fill(windowX + WINDOW_WIDTH - 1, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8); // Right

        // Render messages
        renderMessages(guiGraphics);

        // Render widgets (tabs, input, buttons)
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Channel indicator
        String channelText = "§7Channel: " + currentChannel.getColorCode() + currentChannel.getDisplayName();
        guiGraphics.drawString(font, Component.literal(channelText), windowX + 10, windowY + WINDOW_HEIGHT - 50, 0xFFFFFFFF, false);
    }

    private void renderMessages(GuiGraphics guiGraphics) {
        int messageAreaY = windowY + TAB_HEIGHT + 10;
        int messageAreaHeight = WINDOW_HEIGHT - TAB_HEIGHT - 70;
        int maxMessages = messageAreaHeight / MESSAGE_LINE_HEIGHT;

        List<ChatMessage> messages = ClientChatData.getMessages(currentChannel);
        int totalMessages = messages.size();

        // Calculate visible message range
        int startIndex = Math.max(0, totalMessages - maxMessages - scrollOffset);
        int endIndex = Math.min(totalMessages, startIndex + maxMessages);

        int yPos = messageAreaY;
        for (int i = startIndex; i < endIndex; i++) {
            ChatMessage msg = messages.get(i);
            renderMessage(guiGraphics, msg, yPos);
            yPos += MESSAGE_LINE_HEIGHT;
        }

        // Scroll indicator
        if (totalMessages > maxMessages) {
            int scrollBarX = windowX + WINDOW_WIDTH - 15;
            int scrollBarHeight = messageAreaHeight - 4;
            int scrollBarY = messageAreaY + 2;

            // Background
            guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + 10, scrollBarY + scrollBarHeight, 0xFF333333);

            // Thumb
            float thumbHeight = Math.max(20, scrollBarHeight * ((float) maxMessages / totalMessages));
            float thumbY = scrollBarY + (scrollBarHeight - thumbHeight) * ((float) scrollOffset / (totalMessages - maxMessages));
            guiGraphics.fill(scrollBarX, (int) thumbY, scrollBarX + 10, (int) (thumbY + thumbHeight), 0xFF6B4DB8);
        }
    }

    private void renderMessage(GuiGraphics guiGraphics, ChatMessage msg, int yPos) {
        String formattedMsg = msg.getFormattedMessage();

        // Add timestamp if enabled
        ClientChatData.ChatSettings settings = ClientChatData.getSettings(msg.getChannel());
        if (settings.shouldShowTimestamps()) {
            String timestamp = TIME_FORMAT.format(new Date(msg.getTimestamp()));
            formattedMsg = "§8[" + timestamp + "] §r" + formattedMsg;
        }

        // Render with word wrap if needed
        int maxWidth = WINDOW_WIDTH - 40;
        if (font.width(formattedMsg) > maxWidth) {
            // Simple truncation for now
            formattedMsg = font.plainSubstrByWidth(formattedMsg, maxWidth - 20) + "...";
        }

        guiGraphics.drawString(font, Component.literal(formattedMsg), windowX + 10, yPos, 0xFFFFFFFF, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Send message on Enter
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (messageInput.isFocused() && !messageInput.getValue().trim().isEmpty()) {
                sendMessage();
                return true;
            }
        }

        // Close on Escape
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Scroll through messages
        List<ChatMessage> messages = ClientChatData.getMessages(currentChannel);
        int messageAreaHeight = WINDOW_HEIGHT - TAB_HEIGHT - 70;
        int maxMessages = messageAreaHeight / MESSAGE_LINE_HEIGHT;
        int maxScroll = Math.max(0, messages.size() - maxMessages);

        if (delta > 0) {
            scrollOffset = Math.min(scrollOffset + 3, maxScroll);
        } else {
            scrollOffset = Math.max(scrollOffset - 3, 0);
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
