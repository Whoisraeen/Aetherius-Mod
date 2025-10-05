package com.aetheriusmmorpg.client.ui;

import com.aetheriusmmorpg.client.ClientPartyData;
import com.aetheriusmmorpg.common.party.Party;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

/**
 * HUD overlay that displays party members' health and status.
 */
public class PartyHUD {

    private static final int MEMBER_HEIGHT = 32;
    private static final int MEMBER_WIDTH = 180;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;

    /**
     * Render the party HUD.
     */
    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (!ClientPartyData.hasParty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player localPlayer = minecraft.player;
        if (localPlayer == null) {
            return;
        }

        List<UUID> memberIds = ClientPartyData.getMemberIds();
        List<String> memberNames = ClientPartyData.getMemberNames();

        if (memberIds.isEmpty()) {
            return;
        }

        // Draw party header
        int yOffset = HUD_Y;
        drawPartyHeader(guiGraphics, minecraft, yOffset);
        yOffset += 20;

        // Draw each party member
        for (int i = 0; i < memberIds.size() && i < memberNames.size(); i++) {
            UUID memberId = memberIds.get(i);
            String memberName = memberNames.get(i);
            boolean isLeader = ClientPartyData.getLeaderId().equals(memberId);
            boolean isSelf = localPlayer.getUUID().equals(memberId);

            // Try to get the actual player entity for health data
            Player memberPlayer = null;
            if (isSelf) {
                memberPlayer = localPlayer;
            } else {
                // Try to find the player in the world
                for (Player player : minecraft.level.players()) {
                    if (player.getUUID().equals(memberId)) {
                        memberPlayer = player;
                        break;
                    }
                }
            }

            drawMember(guiGraphics, minecraft, memberName, memberPlayer, isLeader, isSelf, yOffset);
            yOffset += MEMBER_HEIGHT + 2;
        }
    }

    /**
     * Draw party header.
     */
    private static void drawPartyHeader(GuiGraphics guiGraphics, Minecraft minecraft, int y) {
        // Background
        guiGraphics.fill(HUD_X, y, HUD_X + MEMBER_WIDTH, y + 18, 0xAA000000);

        // Border
        guiGraphics.fill(HUD_X, y, HUD_X + MEMBER_WIDTH, y + 1, 0xFF6B4DB8);

        // Party info text
        int memberCount = ClientPartyData.getMemberCount();
        Party.LootMode lootMode = ClientPartyData.getLootMode();

        Component partyText = Component.literal("§6Party §7(" + memberCount + "/10) - " + lootMode.name());
        guiGraphics.drawString(minecraft.font, partyText, HUD_X + 5, y + 5, 0xFFFFFFFF);
    }

    /**
     * Draw a party member.
     */
    private static void drawMember(GuiGraphics guiGraphics, Minecraft minecraft, String name, Player player, boolean isLeader, boolean isSelf, int y) {
        // Background
        int bgColor = isSelf ? 0xAA1A1A3A : 0xAA000000;
        guiGraphics.fill(HUD_X, y, HUD_X + MEMBER_WIDTH, y + MEMBER_HEIGHT, bgColor);

        // Border (gold for leader, white for self, gray for others)
        int borderColor = isLeader ? 0xFFFFAA00 : (isSelf ? 0xFFFFFFFF : 0xFF555555);
        guiGraphics.fill(HUD_X, y, HUD_X + MEMBER_WIDTH, y + 1, borderColor);

        // Name with leader indicator
        String displayName = (isLeader ? "§6★ " : "§f") + name;
        Component nameText = Component.literal(displayName);
        guiGraphics.drawString(minecraft.font, nameText, HUD_X + 5, y + 5, 0xFFFFFFFF);

        // Health bar
        if (player != null) {
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float healthPercent = health / maxHealth;

            // Health bar background
            int barX = HUD_X + 5;
            int barY = y + 18;
            int barWidth = MEMBER_WIDTH - 10;
            int barHeight = 8;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

            // Health bar fill
            int healthWidth = (int) (barWidth * healthPercent);
            int healthColor = getHealthColor(healthPercent);
            guiGraphics.fill(barX, barY, barX + healthWidth, barY + barHeight, healthColor);

            // Health text
            Component healthText = Component.literal(String.format("§f%.0f / %.0f", health, maxHealth));
            int textX = barX + barWidth / 2 - minecraft.font.width(healthText) / 2;
            guiGraphics.drawString(minecraft.font, healthText, textX, barY + 1, 0xFFFFFFFF);
        } else {
            // Player not in range or offline
            Component offlineText = Component.literal("§7Out of range");
            guiGraphics.drawString(minecraft.font, offlineText, HUD_X + 5, y + 18, 0xFF888888);
        }
    }

    /**
     * Get health bar color based on health percentage.
     */
    private static int getHealthColor(float healthPercent) {
        if (healthPercent > 0.5f) {
            return 0xFF00FF00; // Green
        } else if (healthPercent > 0.25f) {
            return 0xFFFFAA00; // Orange
        } else {
            return 0xFFFF0000; // Red
        }
    }
}
