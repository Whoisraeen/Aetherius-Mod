package com.aetheriusmmorpg.client.ui;

import com.aetheriusmmorpg.network.NetworkHandler;
import com.aetheriusmmorpg.network.packet.party.C2SPartyActionPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * Overlay that displays party invitations with Accept/Decline buttons.
 */
public class PartyInviteOverlay {

    private static UUID inviterUUID = null;
    private static String inviterName = null;
    private static long inviteTime = 0;
    private static final long INVITE_DURATION = 30000; // 30 seconds

    /**
     * Show a party invitation.
     */
    public static void showInvite(UUID inviterUUID, String inviterName) {
        PartyInviteOverlay.inviterUUID = inviterUUID;
        PartyInviteOverlay.inviterName = inviterName;
        PartyInviteOverlay.inviteTime = System.currentTimeMillis();

        Minecraft.getInstance().player.sendSystemMessage(
            Component.literal("§6[Party] §f" + inviterName + " has invited you to join their party!")
        );
        Minecraft.getInstance().player.sendSystemMessage(
            Component.literal("§7Press §aY§7 to accept or §cN§7 to decline")
        );
    }

    /**
     * Accept the current invitation.
     */
    public static void acceptInvite() {
        if (hasActiveInvite()) {
            NetworkHandler.sendToServer(new C2SPartyActionPacket(C2SPartyActionPacket.PartyAction.ACCEPT));
            clearInvite();
        }
    }

    /**
     * Decline the current invitation.
     */
    public static void declineInvite() {
        if (hasActiveInvite()) {
            NetworkHandler.sendToServer(new C2SPartyActionPacket(C2SPartyActionPacket.PartyAction.DECLINE));
            clearInvite();
        }
    }

    /**
     * Clear the current invitation.
     */
    public static void clearInvite() {
        inviterUUID = null;
        inviterName = null;
        inviteTime = 0;
    }

    /**
     * Check if there is an active invitation.
     */
    public static boolean hasActiveInvite() {
        if (inviterUUID == null) {
            return false;
        }

        // Check if invite has expired
        if (System.currentTimeMillis() - inviteTime > INVITE_DURATION) {
            clearInvite();
            return false;
        }

        return true;
    }

    /**
     * Render the party invite overlay.
     */
    public static void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (!hasActiveInvite()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int centerX = screenWidth / 2;
        int topY = 50;

        // Background
        int bgWidth = 250;
        int bgHeight = 60;
        int bgX = centerX - bgWidth / 2;
        int bgY = topY;

        // Draw background with transparency
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0xCC000000);

        // Draw border
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + 2, 0xFF6B4DB8); // Top
        guiGraphics.fill(bgX, bgY + bgHeight - 2, bgX + bgWidth, bgY + bgHeight, 0xFF6B4DB8); // Bottom
        guiGraphics.fill(bgX, bgY, bgX + 2, bgY + bgHeight, 0xFF6B4DB8); // Left
        guiGraphics.fill(bgX + bgWidth - 2, bgY, bgX + bgWidth, bgY + bgHeight, 0xFF6B4DB8); // Right

        // Draw text
        Component titleText = Component.literal("§6Party Invitation");
        Component inviteText = Component.literal("§f" + inviterName + " §7invited you to their party");

        guiGraphics.drawCenteredString(minecraft.font, titleText, centerX, bgY + 8, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(minecraft.font, inviteText, centerX, bgY + 22, 0xFFFFFFFF);

        // Time remaining
        long timeLeft = (INVITE_DURATION - (System.currentTimeMillis() - inviteTime)) / 1000;
        Component timeText = Component.literal("§7(" + timeLeft + "s)");
        guiGraphics.drawCenteredString(minecraft.font, timeText, centerX, bgY + 36, 0xFFFFFFFF);

        // Draw instruction text below
        Component instructionText = Component.literal("§7Press §aY§7 to accept or §cN§7 to decline");
        guiGraphics.drawCenteredString(minecraft.font, instructionText, centerX, bgY + bgHeight + 5, 0xFFFFFFFF);
    }

    /**
     * Handle key press for quick accept/decline.
     */
    public static boolean handleKeyPress(int keyCode) {
        if (!hasActiveInvite()) {
            return false;
        }

        // Y key (GLFW_KEY_Y = 89)
        if (keyCode == 89) {
            acceptInvite();
            return true;
        }

        // N key (GLFW_KEY_N = 78)
        if (keyCode == 78) {
            declineInvite();
            return true;
        }

        return false;
    }
}
