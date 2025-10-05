package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.friend.C2SFriendActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * PWI-style add friend dialog.
 */
public class AddFriendDialog extends Screen {

    private static final int DIALOG_WIDTH = 260;
    private static final int DIALOG_HEIGHT = 120;

    private EditBox playerNameBox;
    private Button sendButton;
    private Button cancelButton;

    public AddFriendDialog() {
        super(Component.literal("Add Friend"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int dialogX = centerX - DIALOG_WIDTH / 2;
        int dialogY = centerY - DIALOG_HEIGHT / 2;

        // Player name input
        playerNameBox = new EditBox(font, dialogX + 30, dialogY + 50, 200, 20, Component.literal("Player Name"));
        playerNameBox.setMaxLength(16);
        playerNameBox.setHint(Component.literal("Enter player name..."));
        addRenderableWidget(playerNameBox);

        // Send button
        sendButton = Button.builder(Component.literal("Send Request"),
            btn -> {
                String playerName = playerNameBox.getValue().trim();
                if (!playerName.isEmpty()) {
                    // TODO: Convert name to UUID and send packet
                    NetworkHandler.sendToServer(new C2SFriendActionPacket(
                        C2SFriendActionPacket.FriendAction.SEND_REQUEST,
                        java.util.UUID.randomUUID() // Would lookup by name
                    ));
                    minecraft.player.sendSystemMessage(Component.literal("§aFriend request sent to " + playerName));
                    this.onClose();
                }
            })
            .bounds(dialogX + 20, dialogY + 85, 100, 20)
            .build();
        addRenderableWidget(sendButton);

        // Cancel button
        cancelButton = Button.builder(Component.literal("Cancel"),
            btn -> this.onClose())
            .bounds(dialogX + 140, dialogY + 85, 80, 20)
            .build();
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int dialogX = centerX - DIALOG_WIDTH / 2;
        int dialogY = centerY - DIALOG_HEIGHT / 2;

        // Dialog background
        guiGraphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xE0000000);
        
        // Border
        guiGraphics.fill(dialogX, dialogY, dialogX + DIALOG_WIDTH, dialogY + 1, 0xFF6B4DB8);
        guiGraphics.fill(dialogX, dialogY + DIALOG_HEIGHT - 1, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF6B4DB8);
        guiGraphics.fill(dialogX, dialogY, dialogX + 1, dialogY + DIALOG_HEIGHT, 0xFF6B4DB8);
        guiGraphics.fill(dialogX + DIALOG_WIDTH - 1, dialogY, dialogX + DIALOG_WIDTH, dialogY + DIALOG_HEIGHT, 0xFF6B4DB8);

        // Title
        Component titleText = Component.literal("§6Add Friend");
        guiGraphics.drawString(font, titleText, centerX - font.width(titleText) / 2, dialogY + 15, 0xFFFFFFFF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

