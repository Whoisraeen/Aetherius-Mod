package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.client.ClientPlayerData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * PWI-style player profile screen showing stats and info.
 */
public class PlayerProfileScreen extends Screen {

    private static final int WINDOW_WIDTH = 320;
    private static final int WINDOW_HEIGHT = 400;

    private final Player targetPlayer;
    private int windowX;
    private int windowY;

    public PlayerProfileScreen(Player targetPlayer) {
        super(Component.literal("Player Profile"));
        this.targetPlayer = targetPlayer;
    }

    @Override
    protected void init() {
        this.windowX = (this.width - WINDOW_WIDTH) / 2;
        this.windowY = (this.height - WINDOW_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        // Window background
        guiGraphics.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xE0000000);

        // Border
        guiGraphics.fill(windowX, windowY, windowX + WINDOW_WIDTH, windowY + 1, 0xFF6B4DB8);
        guiGraphics.fill(windowX, windowY + WINDOW_HEIGHT - 1, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8);
        guiGraphics.fill(windowX, windowY, windowX + 1, windowY + WINDOW_HEIGHT, 0xFF6B4DB8);
        guiGraphics.fill(windowX + WINDOW_WIDTH - 1, windowY, windowX + WINDOW_WIDTH, windowY + WINDOW_HEIGHT, 0xFF6B4DB8);

        renderProfile(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderProfile(GuiGraphics guiGraphics) {
        int yPos = windowY + 20;
        int xStart = windowX + 20;

        // Player name
        Component nameText = Component.literal("§6§l" + targetPlayer.getName().getString());
        guiGraphics.drawString(font, nameText, xStart, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        // Basic Info
        guiGraphics.drawString(font, Component.literal("§b═══ Character Info ═══"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        guiGraphics.drawString(font, Component.literal("§7Level: §f" + ClientPlayerData.getLevel()), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        // TODO: Get race/class from ClientPlayerData when methods are available
        guiGraphics.drawString(font, Component.literal("§7Race: §fUnknown"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        guiGraphics.drawString(font, Component.literal("§7Class: §fUnknown"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        // Stats
        guiGraphics.drawString(font, Component.literal("§b═══ Statistics ═══"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        guiGraphics.drawString(font, Component.literal("§7Power: §f" + (int)ClientPlayerData.getPower()), xStart, yPos, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, Component.literal("§7Spirit: §f" + (int)ClientPlayerData.getSpirit()), xStart + 150, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        guiGraphics.drawString(font, Component.literal("§7Agility: §f" + (int)ClientPlayerData.getAgility()), xStart, yPos, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, Component.literal("§7Defense: §f" + (int)ClientPlayerData.getDefense()), xStart + 150, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        guiGraphics.drawString(font, Component.literal("§7Crit Rate: §f" + (int)ClientPlayerData.getCritRate() + "%"), xStart, yPos, 0xFFFFFFFF, false);
        guiGraphics.drawString(font, Component.literal("§7Haste: §f" + (int)ClientPlayerData.getHaste() + "%"), xStart + 150, yPos, 0xFFFFFFFF, false);
        yPos += 20;

        // Experience bar
        guiGraphics.drawString(font, Component.literal("§b═══ Experience ═══"), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 15;

        long exp = ClientPlayerData.getExperience();
        long expNext = ClientPlayerData.getExperienceForNextLevel();
        guiGraphics.drawString(font, Component.literal("§f" + exp + " / " + expNext), xStart, yPos, 0xFFFFFFFF, false);
        yPos += 12;

        // Experience bar
        int barWidth = 260;
        int barHeight = 16;
        guiGraphics.fill(xStart, yPos, xStart + barWidth, yPos + barHeight, 0xFF333333);
        int filledWidth = expNext > 0 ? (int) ((double) exp / expNext * barWidth) : 0;
        guiGraphics.fill(xStart, yPos, xStart + filledWidth, yPos + barHeight, 0xFF6B4DB8);
        
        Component expText = Component.literal("§f" + (expNext > 0 ? (int)((double)exp / expNext * 100) : 0) + "%");
        guiGraphics.drawString(font, expText, xStart + barWidth / 2 - font.width(expText) / 2, yPos + 4, 0xFFFFFFFF, false);
        yPos += 30;

        // Gold
        guiGraphics.drawString(font, Component.literal("§e§lGold: §f" + ClientPlayerData.getGold()), xStart, yPos, 0xFFFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

