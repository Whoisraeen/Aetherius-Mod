package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.AetheriusMod;
import com.aetheriusmmorpg.client.ClientPlayerData;
import com.aetheriusmmorpg.common.menu.CharacterSheetMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Character sheet GUI displaying player stats.
 * Read-only display of server-synced data.
 */
public class CharacterSheetScreen extends AbstractContainerScreen<CharacterSheetMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(AetheriusMod.MOD_ID, "textures/gui/character_sheet.png");

    public CharacterSheetScreen(CharacterSheetMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw background (simple gray rectangle for now since we don't have texture yet)
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        // Level & Experience
        guiGraphics.drawString(this.font,
            Component.literal("Level: " + ClientPlayerData.getLevel()),
            10, 25, 0x404040, false);

        long xp = ClientPlayerData.getExperience();
        long xpNeeded = ClientPlayerData.getExperienceForNextLevel();
        guiGraphics.drawString(this.font,
            Component.literal("XP: " + xp + " / " + xpNeeded),
            10, 35, 0x404040, false);

        // Custom Attributes
        int yOffset = 55;
        guiGraphics.drawString(this.font,
            Component.literal("Power: " + (int)ClientPlayerData.getPower()),
            10, yOffset, 0x404040, false);

        guiGraphics.drawString(this.font,
            Component.literal("Spirit: " + (int)ClientPlayerData.getSpirit()),
            10, yOffset + 10, 0x404040, false);

        guiGraphics.drawString(this.font,
            Component.literal("Agility: " + (int)ClientPlayerData.getAgility()),
            10, yOffset + 20, 0x404040, false);

        guiGraphics.drawString(this.font,
            Component.literal("Defense: " + (int)ClientPlayerData.getDefense()),
            10, yOffset + 30, 0x404040, false);

        guiGraphics.drawString(this.font,
            Component.literal("Crit Rate: " + (int)ClientPlayerData.getCritRate() + "%"),
            10, yOffset + 40, 0x404040, false);

        guiGraphics.drawString(this.font,
            Component.literal("Haste: " + (int)ClientPlayerData.getHaste() + "%"),
            10, yOffset + 50, 0x404040, false);

        // Gold
        guiGraphics.drawString(this.font,
            Component.literal("Gold: " + ClientPlayerData.getGold()),
            10, yOffset + 65, 0xFFD700, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
